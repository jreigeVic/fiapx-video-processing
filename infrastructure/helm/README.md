# Helm - FIAP X Video Processing Platform

Deploys the 4 microservices onto the EKS cluster provisioned by
`infrastructure/terraform`. See ADR-005 (Kubernetes) and ADR-014 (shared
RDS instance) for the architectural decisions behind this layout.

## Chart layout

```
infrastructure/helm/
├── microservice/     # 1 generic chart, installed 4 times (1 release per service)
├── cluster-setup/    # 1 release: namespace, shared Secrets, DB bootstrap Job
└── render-aws-values.sh
```

Each service is deployed as an **independent Helm release** of the same
`microservice` chart, matching the independent deploy cycle required by
HLD-14 (CI/CD) - `helm upgrade identity-service` never touches the other
3 services.

## Architecture decisions specific to this phase

- **No Ingress Controller.** The AWS Load Balancer Controller needs IRSA
  (an EKS OIDC provider + a custom IAM role), both blocked in AWS Academy
  (`infrastructure/terraform/iam.tf`). `identity-service` and
  `video-service` instead use a plain Kubernetes `Service type:
  LoadBalancer`, which EKS's in-tree cloud provider is expected to satisfy
  with a classic ELB using only the node's `LabRole` - no extra controller
  to install. This is a documented deviation from the "Ingress Controller"
  component listed in `docs/HLD/10-deployment-architecture.md`. Not
  verified against a live cluster in this session (no cluster available
  here) - if `LabRole` turns out to lack the ELB permissions, this is the
  first thing to check when validating the first real deploy.
- **No IRSA / no ServiceAccount annotations.** Pods get AWS credentials
  from the EKS node's instance profile (same `LabRole`), exactly like the
  app services already assume when `fiapx.aws.endpoint-override` is blank
  (SDK default credential chain). No static AWS keys anywhere.
- **processing-worker and notification-service have no Service.** Neither
  has an embedded HTTP server (`docs/LLD/processing-worker.md`,
  `docs/LLD/notification-service.md`) - they only poll SQS. Liveness uses
  the same `pgrep -f app.jar` exec probe as `docker-compose.yml`; there is
  no readiness probe because nothing ever routes traffic to them.
- **HPA on CPU**, per ADR-005/HLD-13's initial strategy (KEDA queue-depth
  scaling is a documented future evolution, not built here).

## Prerequisites

1. `terraform apply` already ran (`infrastructure/terraform`) and the EKS
   cluster exists.
2. Point `kubectl`/`helm` at the cluster:
   ```bash
   aws eks update-kubeconfig --name "$(terraform -chdir=infrastructure/terraform output -raw eks_cluster_name)"
   ```
3. **metrics-server** - not shipped by EKS by default, but required for
   the HPAs to compute a CPU target (without it they report
   `<unknown>` and never scale). Install the upstream manifest once per
   cluster:
   ```bash
   kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml
   ```
4. Images pushed to ECR for all 4 services. Dispatch the `build-push-ecr`
   job of `.github/workflows/cd.yml` (`gh workflow run cd.yml`) - it needs
   3 repository secrets refreshed from the active AWS Academy lab session
   (credentials rotate every ~4h): `AWS_ACCESS_KEY_ID`,
   `AWS_SECRET_ACCESS_KEY`, `AWS_SESSION_TOKEN`.
5. Optionally, `NEW_RELIC_LICENSE_KEY` as a repository secret (doesn't
   rotate like the 3 above). When present, the `deploy` job's "Sync New
   Relic license key" step applies it directly to the
   `fiapx-newrelic-license` Kubernetes Secret on every dispatch, ahead of
   the microservice rollout - keeping observability (ADR-015) live without
   a manual `helm upgrade cluster-setup` step. When absent, that step is
   skipped and pods keep whatever value the Secret already has (see step
   comment in `cd.yml` for the Helm/kubectl ownership caveat).

## Deploy order

### 1. Generate the AWS overlay values

```bash
cd infrastructure/helm
./render-aws-values.sh
```

Reads `terraform output` (RDS endpoint, S3 bucket, SES sender, ECR repo
URLs, region) and writes `infrastructure/helm/generated/*.aws.yaml`
(gitignored - contains the RDS master password and a generated JWT
secret). Requires `terraform`, `jq`, and `openssl` on PATH.

### 2. cluster-setup (namespace, secrets, DB bootstrap)

```bash
helm upgrade --install cluster-setup ./cluster-setup \
  -f generated/values-cluster-setup.aws.yaml

kubectl wait --for=condition=complete --timeout=120s job/fiapx-db-bootstrap -n fiapx
```

This creates the `fiapx` namespace, the `fiapx-db-credentials` and
`fiapx-jwt-secret` Secrets, and runs a Job that creates the 4 logical
databases (`auth_db`, `video_db`, `processing_db`, `notification_db`) on
the shared RDS instance (ADR-014) if they don't already exist - reusing
the same idempotent logic as
`infrastructure/docker/postgres/init-databases.sh`. **This must complete
before step 3**: each service's Flyway migration needs its database to
already exist.

### 3. The 4 service releases

```bash
cd microservice

helm upgrade --install identity-service . -n fiapx --create-namespace \
  -f values-identity-service.yaml -f ../generated/values-identity-service.aws.yaml

helm upgrade --install video-service . -n fiapx \
  -f values-video-service.yaml -f ../generated/values-video-service.aws.yaml

helm upgrade --install processing-worker . -n fiapx \
  -f values-processing-worker.yaml -f ../generated/values-processing-worker.aws.yaml

helm upgrade --install notification-service . -n fiapx \
  -f values-notification-service.yaml -f ../generated/values-notification-service.aws.yaml
```

Each pair of `-f` files must be passed **in this order**: Helm merges
maps but replaces lists wholesale across `-f` files, and `env` is only
declared in the generated overlay for this reason (see comments in each
`values-<service>.yaml`).

### Find the public endpoints

```bash
kubectl get svc identity-service video-service -n fiapx
```

## CD (GitHub Actions)

`.github/workflows/cd.yml` is `workflow_dispatch`-only: AWS Academy
session credentials rotate every ~4h, so there is no long-lived AWS
identity to trigger this on push. It has 2 jobs:

- **`build-push-ecr`**: builds each service's jar and pushes its image to
  ECR, tagged by commit SHA and `latest`.
- **`deploy`**: resolves the environment via live AWS CLI lookups
  (deterministic names Terraform assigns - no `terraform output` access
  from CI, since the state is local-only to the operator's machine),
  then `helm upgrade --install`s the 4 microservice releases (tagged by
  commit SHA, not `latest` - traceable rollbacks), and runs an end-to-end
  smoke test (register -> login -> upload a tiny real fixture video via
  `ffmpeg` -> poll status -> assert `PROCESSED` -> download). **Does not**
  deploy `cluster-setup` - see the manual steps above, done once per
  environment.

### Required repository configuration

- **Secrets** (refresh every lab session, before dispatching):
  `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`, `AWS_SESSION_TOKEN`.
- **Variable**: `SES_SENDER_EMAIL` (the same address verified in
  `infrastructure/terraform/terraform.tfvars` / SES, or a placeholder if
  `ses_manage_identities` is disabled per ADR-016).

### Per-lab-session runbook

1. Start the AWS Academy lab session; copy its temporary credentials.
2. Update the 3 GitHub secrets above (`gh secret set NAME` or the repo
   Settings UI).
3. If infrastructure isn't already up: `terraform apply`
   (`infrastructure/terraform`), then the one-time manual steps in
   Prerequisites/Deploy order 1-2 above.
4. Dispatch the pipeline: `gh workflow run cd.yml`.
5. Get the public endpoints from the deploy job's summary, or
   `kubectl get svc identity-service video-service -n fiapx`.
6. Between sessions, EKS/RDS keep accruing AWS Academy budget whether or
   not anything is deployed on them; `terraform destroy` if the budget is
   a concern, `terraform apply` again next session (~15min for EKS to
   come back, then re-run the manual cluster-setup step once, since a new
   cluster starts empty).

## Out of Scope Findings

- `docs/HLD/14-ci-cd.md` lists **GitHub Container Registry** as the
  image registry, but `infrastructure/terraform/ecr.tf` (prior epic)
  provisions **ECR** instead. Pre-existing inconsistency, not introduced
  by this phase - flagging per CLAUDE.md scope protection rather than
  editing HLD docs outside the current task.
