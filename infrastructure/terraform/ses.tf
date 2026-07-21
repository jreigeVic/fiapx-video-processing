# AWS Academy accounts stay in the SES sandbox: the sender AND every
# recipient must be verified. Terraform would create the identities; each
# mailbox would still have to click the confirmation link SES sends before
# the notification-service can deliver to it.
#
# Disabled by default (ADR-016): AWS Academy's voclabs role denies
# ses:VerifyEmailIdentity (AccessDenied), so this resource can never
# succeed in that environment - it would fail every `terraform apply`.
# notification-service already handles the resulting SES rejection
# gracefully at runtime (EmailNotificationAdapter catches
# MessageRejectedException, NotifyVideo*UseCase records the notification
# as FAILED and acks the event - no crash, no retry storm). Set
# var.ses_manage_identities = true only in an account where the IAM
# identity actually has this permission.
resource "aws_ses_email_identity" "identities" {
  for_each = var.ses_manage_identities ? toset(concat([var.ses_sender_email], var.ses_recipient_emails)) : toset([])
  email    = each.value
}
