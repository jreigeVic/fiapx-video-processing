# The lab's default VPC: public subnets with an internet gateway already
# attached. Enough for a short-lived lab cluster and avoids NAT gateway
# cost against the Academy budget.
data "aws_vpc" "default" {
  default = true
}

# EKS rejects control-plane placement in us-east-1e
# (UnsupportedAvailabilityZoneException), so filter to the supported AZs.
data "aws_subnets" "eks" {
  filter {
    name   = "vpc-id"
    values = [data.aws_vpc.default.id]
  }

  filter {
    name   = "availability-zone"
    values = formatlist("%s%s", var.region, ["a", "b", "c", "d", "f"])
  }
}

# Kubernetes Service type=LoadBalancer discovers eligible subnets through
# these tags; without them the controller cannot provision the ELB.
resource "aws_ec2_tag" "subnet_elb_role" {
  for_each    = toset(data.aws_subnets.eks.ids)
  resource_id = each.value
  key         = "kubernetes.io/role/elb"
  value       = "1"
}

resource "aws_ec2_tag" "subnet_cluster_shared" {
  for_each    = toset(data.aws_subnets.eks.ids)
  resource_id = each.value
  key         = "kubernetes.io/cluster/${var.eks_cluster_name}"
  value       = "shared"
}
