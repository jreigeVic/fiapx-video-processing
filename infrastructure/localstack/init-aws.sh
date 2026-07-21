#!/bin/sh
# Provisions the AWS resources the four microservices expect, inside
# LocalStack. Runs automatically on container start (mounted into
# /etc/localstack/init/ready.d/ - see docker-compose.yml).
#
# Naming (kebab-case, matching ADR-012): one S3 bucket with prefixes
# (videos/original/, videos/results/) per docs/diagrams/event-catalog.md's
# payload examples; one SNS topic per domain event; one SQS queue per
# consumer, each with its own DLQ, matching the topology diagram in
# docs/diagrams/event-catalog.md.
set -e

REGION="${AWS_DEFAULT_REGION:-us-east-1}"
BUCKET_NAME="fiapx-videos"
SENDER_EMAIL="no-reply@fiapx.local"

echo "[init-aws] Creating S3 bucket: $BUCKET_NAME"
awslocal s3api create-bucket --bucket "$BUCKET_NAME" --region "$REGION" >/dev/null 2>&1 || true

create_queue_with_dlq() {
  queue_name="$1"
  dlq_name="${queue_name}-dlq"

  awslocal sqs create-queue --queue-name "$dlq_name" >/dev/null
  dlq_arn=$(awslocal sqs get-queue-attributes \
    --queue-url "$(awslocal sqs get-queue-url --queue-name "$dlq_name" --query QueueUrl --output text)" \
    --attribute-names QueueArn --query 'Attributes.QueueArn' --output text)

  redrive_policy="{\"deadLetterTargetArn\":\"$dlq_arn\",\"maxReceiveCount\":\"5\"}"
  awslocal sqs create-queue --queue-name "$queue_name" \
    --attributes "{\"RedrivePolicy\":\"$(echo "$redrive_policy" | sed 's/"/\\"/g')\"}" >/dev/null
}

echo "[init-aws] Creating SQS queues and DLQs"
create_queue_with_dlq "video-processing-queue"
create_queue_with_dlq "video-results-queue"
create_queue_with_dlq "notification-queue"

# Must exceed the worker's 10-minute ffmpeg timeout: with the 30s default,
# an in-flight message reappears on the queue mid-processing and another
# poller (or replica) starts processing the same video, and legitimate slow
# jobs burn through maxReceiveCount into the DLQ. Mirrors the
# VisibilityTimeout Terraform sets on the real AWS queue. set-queue-attributes
# (rather than create-queue attributes) keeps this idempotent against
# LocalStack volumes persisted before this setting existed.
echo "[init-aws] Raising video-processing-queue visibility timeout to 900s"
awslocal sqs set-queue-attributes \
  --queue-url "$(awslocal sqs get-queue-url --queue-name "video-processing-queue" --query QueueUrl --output text)" \
  --attributes '{"VisibilityTimeout":"900"}' >/dev/null

queue_arn() {
  awslocal sqs get-queue-attributes \
    --queue-url "$(awslocal sqs get-queue-url --queue-name "$1" --query QueueUrl --output text)" \
    --attribute-names QueueArn --query 'Attributes.QueueArn' --output text
}

VIDEO_PROCESSING_QUEUE_ARN=$(queue_arn "video-processing-queue")
VIDEO_RESULTS_QUEUE_ARN=$(queue_arn "video-results-queue")
NOTIFICATION_QUEUE_ARN=$(queue_arn "notification-queue")

allow_sns_to_send() {
  queue_name="$1"
  queue_arn="$2"
  queue_url=$(awslocal sqs get-queue-url --queue-name "$queue_name" --query QueueUrl --output text)
  policy="{\"Version\":\"2012-10-17\",\"Statement\":[{\"Effect\":\"Allow\",\"Principal\":\"*\",\"Action\":\"sqs:SendMessage\",\"Resource\":\"$queue_arn\"}]}"
  awslocal sqs set-queue-attributes --queue-url "$queue_url" \
    --attributes "{\"Policy\":\"$(echo "$policy" | sed 's/"/\\"/g')\"}" >/dev/null
}

allow_sns_to_send "video-processing-queue" "$VIDEO_PROCESSING_QUEUE_ARN"
allow_sns_to_send "video-results-queue" "$VIDEO_RESULTS_QUEUE_ARN"
allow_sns_to_send "notification-queue" "$NOTIFICATION_QUEUE_ARN"

echo "[init-aws] Creating SNS topics"
VIDEO_UPLOADED_TOPIC_ARN=$(awslocal sns create-topic --name video-uploaded --query TopicArn --output text)
VIDEO_PROCESSED_TOPIC_ARN=$(awslocal sns create-topic --name video-processed --query TopicArn --output text)
VIDEO_FAILED_TOPIC_ARN=$(awslocal sns create-topic --name video-failed --query TopicArn --output text)

echo "[init-aws] Subscribing queues to topics (raw message delivery)"
awslocal sns subscribe --topic-arn "$VIDEO_UPLOADED_TOPIC_ARN" --protocol sqs \
  --notification-endpoint "$VIDEO_PROCESSING_QUEUE_ARN" \
  --attributes RawMessageDelivery=true >/dev/null

awslocal sns subscribe --topic-arn "$VIDEO_PROCESSED_TOPIC_ARN" --protocol sqs \
  --notification-endpoint "$VIDEO_RESULTS_QUEUE_ARN" \
  --attributes RawMessageDelivery=true >/dev/null
awslocal sns subscribe --topic-arn "$VIDEO_PROCESSED_TOPIC_ARN" --protocol sqs \
  --notification-endpoint "$NOTIFICATION_QUEUE_ARN" \
  --attributes RawMessageDelivery=true >/dev/null

awslocal sns subscribe --topic-arn "$VIDEO_FAILED_TOPIC_ARN" --protocol sqs \
  --notification-endpoint "$VIDEO_RESULTS_QUEUE_ARN" \
  --attributes RawMessageDelivery=true >/dev/null
awslocal sns subscribe --topic-arn "$VIDEO_FAILED_TOPIC_ARN" --protocol sqs \
  --notification-endpoint "$NOTIFICATION_QUEUE_ARN" \
  --attributes RawMessageDelivery=true >/dev/null

echo "[init-aws] Verifying SES sender identity: $SENDER_EMAIL"
awslocal ses verify-email-identity --email-address "$SENDER_EMAIL" >/dev/null

echo "[init-aws] Done."
