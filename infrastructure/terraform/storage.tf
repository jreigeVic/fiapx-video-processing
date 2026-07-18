# S3 bucket names are global, so a random suffix keeps the well-known
# local name (fiapx-videos) collision-free in real AWS. Services receive
# the full name through the VIDEO_S3_BUCKET env var.
resource "random_id" "bucket_suffix" {
  byte_length = 4
}

resource "aws_s3_bucket" "videos" {
  bucket        = "fiapx-videos-${random_id.bucket_suffix.hex}"
  force_destroy = true # lab environment - allow terraform destroy with objects present
}

# Download links are presigned URLs, which work with all public access
# blocked - nothing in this bucket is ever public.
resource "aws_s3_bucket_public_access_block" "videos" {
  bucket                  = aws_s3_bucket.videos.id
  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}
