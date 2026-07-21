provider "aws" {
  region = var.region

  default_tags {
    tags = {
      Project   = "fiapx-video-processing"
      ManagedBy = "terraform"
    }
  }
}
