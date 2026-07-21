output "region" {
  value = var.region
}

output "ecr_repository_urls" {
  description = "Push targets for the CD workflow (docker push)"
  value       = { for name, repo in aws_ecr_repository.services : name => repo.repository_url }
}

output "eks_cluster_name" {
  description = "For aws eks update-kubeconfig"
  value       = aws_eks_cluster.this.name
}

output "s3_bucket_name" {
  description = "Value for the services' VIDEO_S3_BUCKET env var"
  value       = aws_s3_bucket.videos.bucket
}

output "rds_endpoint" {
  description = "host:port for the services' SPRING_DATASOURCE_URL"
  value       = aws_db_instance.postgres.endpoint
}

output "rds_username" {
  value = var.db_username
}

output "rds_password" {
  value     = random_password.db.result
  sensitive = true
}

output "sns_topic_arns" {
  value = { for name, topic in aws_sns_topic.topics : name => topic.arn }
}

output "sqs_queue_urls" {
  value = { for name, queue in aws_sqs_queue.queues : name => queue.url }
}

output "ses_sender_email" {
  description = "Value for the notification-service's NOTIFICATION_SENDER_EMAIL env var"
  value       = var.ses_sender_email
}
