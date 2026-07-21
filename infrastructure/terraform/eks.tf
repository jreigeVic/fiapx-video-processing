resource "aws_eks_cluster" "this" {
  name     = var.eks_cluster_name
  role_arn = data.aws_iam_role.lab_role.arn
  version  = var.eks_version

  vpc_config {
    subnet_ids = data.aws_subnets.eks.ids
  }

  access_config {
    authentication_mode                         = "API_AND_CONFIG_MAP"
    bootstrap_cluster_creator_admin_permissions = true
  }
}

resource "aws_eks_node_group" "default" {
  cluster_name    = aws_eks_cluster.this.name
  node_group_name = "fiapx-default"
  node_role_arn   = data.aws_iam_role.lab_role.arn
  subnet_ids      = data.aws_subnets.eks.ids
  instance_types  = [var.node_instance_type]

  scaling_config {
    min_size     = var.node_scaling.min
    desired_size = var.node_scaling.desired
    max_size     = var.node_scaling.max
  }
}

# CloudWatch Container Insights (ADR-008/ADR-015: infra monitoring pillar).
# No service_account_role_arn set - no IRSA/OIDC provider in AWS Academy
# (infrastructure/terraform/iam.tf), so the addon's pods run under the
# node instance profile (LabRole), the same credential path every app pod
# already uses.
resource "aws_eks_addon" "cloudwatch_observability" {
  cluster_name = aws_eks_cluster.this.name
  addon_name   = "amazon-cloudwatch-observability"

  depends_on = [aws_eks_node_group.default]
}
