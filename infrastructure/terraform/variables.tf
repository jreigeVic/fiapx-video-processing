variable "region" {
  description = "AWS region (AWS Academy labs run in us-east-1)"
  type        = string
  default     = "us-east-1"
}

variable "lab_role_name" {
  description = "Pre-existing IAM role provided by AWS Academy (role creation is blocked in the lab)"
  type        = string
  default     = "LabRole"
}

variable "eks_cluster_name" {
  description = "EKS cluster name"
  type        = string
  default     = "fiapx-eks"
}

variable "eks_version" {
  description = "EKS Kubernetes version; null uses the current EKS default (always in standard support, avoiding extended-support pricing)"
  type        = string
  default     = null
}

variable "node_instance_type" {
  description = "Worker node instance type (must be within AWS Academy's allowed sizes)"
  type        = string
  default     = "t3.medium"
}

variable "node_scaling" {
  description = "EKS managed node group scaling bounds (max leaves headroom for HPA to add pods)"
  type = object({
    min     = number
    desired = number
    max     = number
  })
  default = {
    min     = 2
    desired = 2
    max     = 4
  }
}

variable "db_instance_class" {
  description = "RDS instance class (single instance hosts the four logical databases - see ADR-014)"
  type        = string
  default     = "db.t3.micro"
}

variable "db_username" {
  description = "RDS master username"
  type        = string
  default     = "fiapx"
}

variable "processing_queue_visibility_timeout_seconds" {
  description = "Must exceed the worker's ffmpeg timeout (10 min) so in-flight messages are not redelivered to competing worker replicas"
  type        = number
  default     = 900
}

variable "ses_sender_email" {
  description = "Sender identity to verify in SES (Academy accounts stay in SES sandbox: sender AND recipients must be verified)"
  type        = string
}

variable "ses_recipient_emails" {
  description = "Recipient identities to verify in SES sandbox (the demo users who will receive notifications)"
  type        = list(string)
  default     = []
}

variable "ses_manage_identities" {
  description = "Whether Terraform creates/verifies SES email identities. Disabled by default: AWS Academy's voclabs role denies ses:VerifyEmailIdentity (AccessDenied) - see ADR-016. Set true only in an account where the IAM identity has this permission."
  type        = bool
  default     = false
}

variable "new_relic_account_id" {
  description = "New Relic account ID (Account > API keys), used by the newrelic provider and the dashboard-as-code resource in observability.tf"
  type        = number
}

variable "new_relic_api_key" {
  description = "New Relic User API key (NRAK-...), distinct from the ingest License Key used by the OTel exporters (see cd.yml) - grants read/write on dashboards via NerdGraph"
  type        = string
  sensitive   = true
}
