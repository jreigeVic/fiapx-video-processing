# AWS Academy blocks IAM role creation, so everything that needs a role
# (EKS cluster, node group) reuses the lab-provided LabRole. Pods inherit
# AWS credentials from the node instance profile - no IRSA, since creating
# an OIDC provider is blocked in the lab too. The app services already use
# the SDK default credential chain when fiapx.aws.endpoint-override is
# blank, so no static keys reach any container.
data "aws_iam_role" "lab_role" {
  name = var.lab_role_name
}
