locals {
  services = [
    "identity-service",
    "video-service",
    "processing-worker",
    "notification-service",
  ]
}

resource "aws_ecr_repository" "services" {
  for_each     = toset(local.services)
  name         = "fiapx/${each.value}"
  force_delete = true # lab environment - allow terraform destroy with images present

  image_scanning_configuration {
    scan_on_push = false # Trivy already scans every image in CI
  }
}
