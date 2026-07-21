#!/bin/bash
set -e

# Bridges infrastructure/terraform outputs into the Helm values this chart
# set needs, without hardcoding any environment-specific value (RDS
# endpoint, random-suffixed S3 bucket name, JWT secret) into committed
# files. Run after `terraform apply`, before installing any Helm release.
#
# Usage: ./render-aws-values.sh
# Output: infrastructure/helm/generated/ (gitignored)

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TF_DIR="$SCRIPT_DIR/../terraform"
OUT_DIR="$SCRIPT_DIR/generated"
mkdir -p "$OUT_DIR"

echo "Reading terraform outputs from $TF_DIR..."
REGION=$(terraform -chdir="$TF_DIR" output -raw region)
RDS_ENDPOINT=$(terraform -chdir="$TF_DIR" output -raw rds_endpoint) # host:port
RDS_HOST="${RDS_ENDPOINT%%:*}"
RDS_PORT="${RDS_ENDPOINT##*:}"
RDS_USERNAME=$(terraform -chdir="$TF_DIR" output -raw rds_username)
RDS_PASSWORD=$(terraform -chdir="$TF_DIR" output -raw rds_password)
S3_BUCKET=$(terraform -chdir="$TF_DIR" output -raw s3_bucket_name)
SES_SENDER=$(terraform -chdir="$TF_DIR" output -raw ses_sender_email)

IDENTITY_ECR=$(terraform -chdir="$TF_DIR" output -json ecr_repository_urls | jq -r '.["identity-service"]')
VIDEO_ECR=$(terraform -chdir="$TF_DIR" output -json ecr_repository_urls | jq -r '.["video-service"]')
WORKER_ECR=$(terraform -chdir="$TF_DIR" output -json ecr_repository_urls | jq -r '.["processing-worker"]')
NOTIFICATION_ECR=$(terraform -chdir="$TF_DIR" output -json ecr_repository_urls | jq -r '.["notification-service"]')

# JWT secret is generated once and reused on every re-run: identity-service
# and video-service must keep agreeing on the same signing key across
# redeploys, or previously issued tokens stop validating.
JWT_SECRET_FILE="$OUT_DIR/.jwt-secret"
if [ -f "$JWT_SECRET_FILE" ]; then
  JWT_SECRET=$(cat "$JWT_SECRET_FILE")
else
  JWT_SECRET=$(openssl rand -base64 48)
  echo "$JWT_SECRET" > "$JWT_SECRET_FILE"
fi

cat > "$OUT_DIR/values-cluster-setup.aws.yaml" <<EOF
namespace: fiapx
db:
  host: "$RDS_HOST"
  port: $RDS_PORT
  masterUser: "$RDS_USERNAME"
  masterPassword: "$RDS_PASSWORD"
jwt:
  secret: "$JWT_SECRET"
EOF

cat > "$OUT_DIR/values-identity-service.aws.yaml" <<EOF
image:
  repository: "$IDENTITY_ECR"
env:
  - name: SPRING_DATASOURCE_URL
    value: "jdbc:postgresql://$RDS_HOST:$RDS_PORT/auth_db"
  - name: AWS_REGION
    value: "$REGION"
EOF

cat > "$OUT_DIR/values-video-service.aws.yaml" <<EOF
image:
  repository: "$VIDEO_ECR"
env:
  - name: SPRING_DATASOURCE_URL
    value: "jdbc:postgresql://$RDS_HOST:$RDS_PORT/video_db"
  - name: AWS_REGION
    value: "$REGION"
  - name: VIDEO_S3_BUCKET
    value: "$S3_BUCKET"
EOF

cat > "$OUT_DIR/values-processing-worker.aws.yaml" <<EOF
image:
  repository: "$WORKER_ECR"
env:
  - name: SPRING_DATASOURCE_URL
    value: "jdbc:postgresql://$RDS_HOST:$RDS_PORT/processing_db"
  - name: AWS_REGION
    value: "$REGION"
  - name: VIDEO_S3_BUCKET
    value: "$S3_BUCKET"
EOF

cat > "$OUT_DIR/values-notification-service.aws.yaml" <<EOF
image:
  repository: "$NOTIFICATION_ECR"
env:
  - name: SPRING_DATASOURCE_URL
    value: "jdbc:postgresql://$RDS_HOST:$RDS_PORT/notification_db"
  - name: AWS_REGION
    value: "$REGION"
  - name: NOTIFICATION_SENDER_EMAIL
    value: "$SES_SENDER"
EOF

echo "Wrote overlay values to $OUT_DIR/"
