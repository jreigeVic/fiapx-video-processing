# Infrastructure

Infra-as-code for the FIAP X platform on AWS Academy (Learner Lab), plus
the LocalStack provisioning used by local development and CI.

| Directory | Purpose |
|---|---|
| `terraform/` | AWS provisioning: ECR, RDS, S3, SNS/SQS (+DLQs), SES, EKS |
| `helm/` | Kubernetes deployment charts (Helm) |
| `kubernetes/` | Cluster bootstrap manifests and scripts |
| `localstack/` | LocalStack init script used by docker-compose and CI |
| `docker/` | Local-development container support (Postgres init) |

## AWS Academy constraints (why the Terraform looks the way it does)

- **No IAM creation**: the lab blocks `iam:CreateRole`. Everything that
  needs a role (EKS cluster, node group) reuses the lab-provided
  **LabRole** via a data source.
- **No IRSA**: creating an OIDC provider is blocked too, so pods inherit
  AWS credentials from the **node instance profile**. The services already
  use the SDK default credential chain when `fiapx.aws.endpoint-override`
  is blank, so no static keys reach any container.
- **Session credentials rotate** every ~4h lab session (access key, secret
  and session token). Export fresh ones before every terraform/kubectl run.
- **Region**: `us-east-1` only.
- **SES sandbox**: sender **and** recipients must be verified email
  identities. Terraform creates the identities; each mailbox still has to
  click the confirmation link SES sends.
- **Single RDS instance** hosts the four logical databases (`auth_db`,
  `video_db`, `processing_db`, `notification_db`) for budget reasons —
  database-per-service (ADR-004) holds at the logical level (see ADR-014).

## Provisioning runbook

1. Start the Learner Lab and open **AWS Details → AWS CLI** to get session
   credentials.
2. Export them (PowerShell shown; use `export` on bash):

   ```powershell
   $env:AWS_ACCESS_KEY_ID     = "..."
   $env:AWS_SECRET_ACCESS_KEY = "..."
   $env:AWS_SESSION_TOKEN     = "..."
   $env:AWS_DEFAULT_REGION    = "us-east-1"
   ```

3. Provision:

   ```sh
   cd infrastructure/terraform
   cp terraform.tfvars.example terraform.tfvars   # fill in the SES emails
   terraform init
   terraform plan
   terraform apply
   ```

4. Read the outputs that feed the Helm values and the CD workflow:

   ```sh
   terraform output                     # everything non-sensitive
   terraform output -raw rds_password   # DB password for the K8s secret
   ```

5. Configure kubectl:

   ```sh
   aws eks update-kubeconfig --name fiapx-eks --region us-east-1
   ```

Terraform state stays **local** (`terraform.tfstate`, gitignored): the lab
is ephemeral and a remote-state backend would add setup for no benefit
here. Keep the state file between sessions — the lab account keeps its
resources when a session ends (only the credentials expire), so the same
state supports incremental applies and `terraform destroy`.

## Queue topology (mirror of `localstack/init-aws.sh`)

| Topic | → Queue | Consumer |
|---|---|---|
| `video-uploaded` | `video-processing-queue` | processing-worker |
| `video-processed` | `video-results-queue` | video-service |
| `video-processed` | `notification-queue` | notification-service |
| `video-failed` | `video-results-queue` | video-service |
| `video-failed` | `notification-queue` | notification-service |

Every queue has a `<name>-dlq` dead-letter queue (`maxReceiveCount: 5`),
and all subscriptions use raw message delivery. `video-processing-queue`
uses a **15-minute visibility timeout**: ffmpeg jobs run up to 10 minutes,
and with multiple worker replicas (competing consumers) a shorter timeout
would redeliver in-flight messages to other replicas — duplicating work
and pushing legitimately slow jobs into the DLQ.
