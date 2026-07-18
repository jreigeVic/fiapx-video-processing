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
