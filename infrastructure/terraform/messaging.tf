# SNS topics, SQS queues and the fan-out between them - the exact same
# topology infrastructure/localstack/init-aws.sh provisions for local
# development and CI (docs/diagrams/event-catalog.md).
locals {
  # Queue name -> visibility timeout. Only the processing queue needs a
  # long timeout: ffmpeg jobs run up to 10 minutes, and with multiple
  # worker replicas (competing consumers) a short timeout would redeliver
  # in-flight messages to other replicas, duplicating work and triggering
  # false DLQ redrives. The other consumers do quick DB/SES work.
  queues = {
    "video-processing-queue" = var.processing_queue_visibility_timeout_seconds
    "video-results-queue"    = 30
    "notification-queue"     = 30
  }

  topics = ["video-uploaded", "video-processed", "video-failed"]

  subscriptions = [
    { topic = "video-uploaded", queue = "video-processing-queue" },
    { topic = "video-processed", queue = "video-results-queue" },
    { topic = "video-processed", queue = "notification-queue" },
    { topic = "video-failed", queue = "video-results-queue" },
    { topic = "video-failed", queue = "notification-queue" },
  ]

  # Queue name -> topic ARNs allowed to send to it, for the queue policies.
  queue_sources = {
    for queue in keys(local.queues) :
    queue => [for s in local.subscriptions : aws_sns_topic.topics[s.topic].arn if s.queue == queue]
  }
}

resource "aws_sns_topic" "topics" {
  for_each = toset(local.topics)
  name     = each.value
}

resource "aws_sqs_queue" "dlq" {
  for_each                  = local.queues
  name                      = "${each.key}-dlq"
  message_retention_seconds = 1209600 # 14 days, to inspect dead-lettered events
}

resource "aws_sqs_queue" "queues" {
  for_each                   = local.queues
  name                       = each.key
  visibility_timeout_seconds = each.value

  redrive_policy = jsonencode({
    deadLetterTargetArn = aws_sqs_queue.dlq[each.key].arn
    maxReceiveCount     = 5
  })
}

resource "aws_sqs_queue_policy" "allow_sns" {
  for_each  = local.queues
  queue_url = aws_sqs_queue.queues[each.key].id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect    = "Allow"
      Principal = { Service = "sns.amazonaws.com" }
      Action    = "sqs:SendMessage"
      Resource  = aws_sqs_queue.queues[each.key].arn
      Condition = { ArnEquals = { "aws:SourceArn" = local.queue_sources[each.key] } }
    }]
  })
}

# Raw message delivery matches the consumers, which parse the event
# envelope straight from the SQS message body.
resource "aws_sns_topic_subscription" "fanout" {
  for_each = { for s in local.subscriptions : "${s.topic}-to-${s.queue}" => s }

  topic_arn            = aws_sns_topic.topics[each.value.topic].arn
  protocol             = "sqs"
  endpoint             = aws_sqs_queue.queues[each.value.queue].arn
  raw_message_delivery = true
}
