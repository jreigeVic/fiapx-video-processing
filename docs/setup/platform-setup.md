# Platform Setup

This document describes the external platforms used by the FIAP X Video Processing Platform, their purpose and how they are used across the project lifecycle.

No cloud infrastructure, CI/CD pipelines or application code are created by this document. It only establishes the configuration reference for the platforms below.

---

## GitHub

**Purpose:** Source control and collaboration for the project.

**Usage:**

- Hosts the `fiapx-video-processing` repository.
- Pull requests are the required path for merging changes.
- Branch protection and review rules apply to the default branch.

---

## GitHub Container Registry (GHCR)

**Purpose:** Container image registry for the microservices defined under `services/`.

**Usage:**

- Each service image is built from its `Dockerfile` and published as `ghcr.io/<owner>/<service-name>`.
- Authentication uses a GitHub Personal Access Token (or `GITHUB_TOKEN` in Actions) with `write:packages` scope.
- Image publishing itself is executed by CI/CD, which is out of scope for this task.

---

## SonarCloud

**Purpose:** Static code analysis and quality gate, per ADR-010.

**Usage:**

- One SonarCloud project is configured per service (or one project covering the monorepo, per organization convention).
- Coverage reports (Jacoco) and static analysis results are submitted using a SonarCloud token.
- Quality gate results are used as a merge criterion once CI/CD is implemented.

---

## New Relic

**Purpose:** Application Performance Monitoring (APM), logs, metrics and distributed tracing, per ADR-008.

**Usage:**

- Services are instrumented with OpenTelemetry and export telemetry to New Relic.
- A New Relic license key authorizes ingestion for the account.
- CloudWatch remains the source of AWS infrastructure metrics; New Relic concentrates application-level observability.

---

## AWS Academy Lab

**Purpose:** Validation environment only, per ADR-002 and ADR-009.

**Usage:**

- Provides temporary, session-based AWS credentials (access key, secret key and session token) that expire when the lab session ends.
- Used exclusively to validate application behavior against real AWS services (S3, SNS, SQS) during development.
- No AWS resources, Terraform, CloudFormation, IAM policies or AWS CLI profiles are created as part of this task.

---

## Secrets Handling

- No real credentials are stored in this repository.
- `.env.example` documents required variable names using placeholders only.
- Actual values are provided locally (developer machine) or via the platform's own secret store (e.g. GitHub Actions secrets) when CI/CD is implemented in a future task.
