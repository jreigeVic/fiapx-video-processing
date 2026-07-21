# AWS Academy accounts stay in the SES sandbox: the sender AND every
# recipient must be verified. Terraform creates the identities; each
# mailbox still has to click the confirmation link SES sends before the
# notification-service can deliver to it.
resource "aws_ses_email_identity" "identities" {
  for_each = toset(concat([var.ses_sender_email], var.ses_recipient_emails))
  email    = each.value
}
