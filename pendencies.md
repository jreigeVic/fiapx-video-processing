# Pendencies — Architecture Readiness Review (TASK-002.6)

This document records findings from the TASK-002.6 Architecture Readiness Review that are **valid but explicitly out of scope** for that task. They do not block TASK-003 (Identity Service) and must **not** be implemented without a dedicated, explicitly approved task.

---

## Package Structure

The package layout described in `ADR-011-microservice-scaffolding.md` and `docs/LLD/shared-architecture.md` does not match the layout actually scaffolded in the four services:

- Docs specify `application/port/in` and `application/port/out` (singular "port"); actual source uses `application/ports/in` and `application/ports/out` (plural) in all four services.
- Docs specify `infrastructure/adapter/{in,out}/{web,persistence,storage,messaging}` (nested by concern); actual source has flat `infrastructure/adapter/in/` and `infrastructure/adapter/out/` with no sub-packages, plus ad-hoc top-level packages not in the docs: `infrastructure/messaging/` (identity-service, video-service, processing-worker), `infrastructure/repository/` (identity-service, video-service, processing-worker), and `configuration/` (all four) instead of the documented `infrastructure/config`.
- A documented `shared/error` / `shared/observability` package group exists in no service.
- The four services are not structurally identical to each other: messaging-adapter location differs per service (identity/video/processing-worker use `infrastructure/messaging`, notification-service uses `infrastructure/adapter/in/messaging`, processing-worker has both simultaneously); notification-service is missing several placeholder package folders the other three services all have (`api/controller`, `domain/model`, `infrastructure/adapter/out`, `infrastructure/repository`).

**Resolution needed**: either update the ADR/LLD package conventions to match the structure already in use, or restructure the four services' packages to match the documented convention — this is an architectural decision, not a typo fix.

---

## Documentation Architecture

- Root `README.md` has no run instructions, no service/port list, and no links into `docs/` or `CONTRIBUTING.md` — a new reader landing there first has no path into the rest of the documentation set.
- No API versioning scheme (e.g. `/v1`) is defined anywhere in `docs/api/` — consistent across all docs today, so not a defect, but worth deciding before the first real endpoint ships.
- No OpenTelemetry/Micrometer-tracing dependency exists in any `build.gradle.kts` yet, despite ADR-008 and several LLDs listing OpenTelemetry as a dependency — likely intentionally deferred to implementation phase, listed here for visibility.

---

## ADR Numbering — Resolved 2026-07-17

`ADR-011-naming-conventions.md` was renumbered to `docs/ADR/ADR-012-naming-conventions.md` to resolve the collision with `ADR-011-microservice-scaffolding.md`. Its date placeholder was filled (`2026-07-13`, matching the sibling scaffolding ADR from the same TASK-002 effort), its `identity_db` example was corrected to `auth_db` to match every other artifact, and the `UserCreated`/`UserAuthenticated`/`NotificationSent` example events were removed since they appeared nowhere else and contradicted `docs/LLD/identity-service.md` (Identity Service publishes no events).

Remaining minor item, not addressed here: several docs (`docs/shared/package-conventions.md`, `event-conventions.md`, `api-standards.md`, and each service's LLD "Traceability" table) still cite a bare "ADR-011" without specifying whether they mean the scaffolding or naming-conventions decision — both now have distinct numbers (011/012) but the prose wasn't audited to point at the correct one. Low risk since both ADRs cover overlapping ground (scaffolding ADR-011 also has a naming-conventions section); worth a pass if it causes confusion in practice.

---

## Service Dependencies — Resolved 2026-07-18

All three gaps below were closed as part of their respective implementation epics (Epic 004 Video Service, Epic 005 Processing Worker, Epic 006 Notification Service): `spring-boot-starter-security` + `jjwt` were added to video-service; `spring-boot-starter-data-jpa` + `org.postgresql:postgresql` + `flyway-database-postgresql` were added to processing-worker and notification-service. Kept for history:

### Notification Service (resolved)

`docs/LLD/notification-service.md` mandates a `JpaNotificationRepositoryAdapter` and a `notification_db` (with a full ER model for `notifications` and `processed_events`), but `services/notification-service/build.gradle.kts` had `flyway-core` with no `spring-boot-starter-data-jpa` and no `org.postgresql:postgresql` driver. The build could not support the persistence layer its own LLD required.

### Video Service (resolved)

`docs/LLD/video-service.md` explicitly requires token validation (401 on missing/invalid token) and lists "Spring Security" as a dependency, but `services/video-service/build.gradle.kts` had neither `spring-boot-starter-security` nor a JWT library (`identity-service` has both `spring-boot-starter-security` and `jjwt`; `video-service` had neither).

(The `ApplicationUnitTest` Flyway/PostgreSQL-16 failure previously tracked here was resolved by commit `d4bea9e` — the test now runs against H2 with Flyway disabled via `application-test.yml`. Reconfirmed passing during epic-roadmap prep on 2026-07-17.)

### Processing Worker (resolved)

`services/processing-worker/build.gradle.kts` had `flyway-core` with no JPA/Postgres driver. Lower risk than the Notification Service gap, since `docs/LLD/processing-worker.md` treats local persistence as optional ("caso utilize persistencia local para idempotencia") — implemented anyway per the idempotency requirement in the same LLD's error-handling table.

---

## `flyway:` key placed at document root instead of under `spring:` — all four services (found 2026-07-18)

Same issue already noted below for identity-service now exists identically in video-service, processing-worker, and notification-service's `application.yml` (each copied the same top-level `flyway:` block when scaffolded during their respective epics). Still a no-op in all four, still harmless only because the values match Spring Boot's defaults. Not fixed as part of Epics 004/005/006 per Scope Protection — a single across-the-board fix (moving `flyway:` under `spring:` in all four files) would be a good small follow-up task.

---

## Identity Service — Minor Config Placement (found during epic/003-identity, 2026-07-17)

`services/identity-service/src/main/resources/application.yml` declares `flyway:` at the document root instead of nested under `spring:`. Spring Boot's `FlywayProperties` binds to `spring.flyway.*`, so this key is currently a no-op — harmless only because the values (`enabled: true`, `locations: classpath:db/migration`) match Spring Boot's own defaults. Not fixed as part of epic/003-identity since it doesn't block the task; worth a one-line fix whenever that file is next touched for an unrelated reason.

---

## Operational Blockers — Final Roadmap Execution (Epics 008-017)

Per standing execution rule (approved 2026-07-19): operational blockers never stop roadmap execution. Each is documented here, its task is marked Blocked/Pending Validation (never completed), and work continues to the next roadmap item that doesn't depend on it. All entries below are reviewed together at Epic 017 (Final Release) consolidation.

### [Epic 009 - Kubernetes] Cannot push the 4 service images to ECR from this session

**Status (2026-07-21): root cause resolved by merge, execution still pending.** PR #15 merged the full epic/007-013 stack into `main`, so `cd.yml` is now a registered, dispatchable workflow (confirmed via `gh workflow list`). What remains is simply running it with fresh AWS Academy session credentials.

**Epic/Task afetadas:** Epic 009 (Kubernetes) - task #20, specifically installing the 4 `microservice` Helm releases (identity-service, video-service, processing-worker, notification-service). Blocks the same step in Epic 010 (CD Pipeline) if not resolved first.

**Descrição do bloqueio:** The 4 Helm releases need their image already pushed to ECR (`infrastructure/terraform/ecr.tf`). No local Docker daemon is available in this session (`docker info` fails) to build and push directly, but this is no longer required now that `cd.yml`'s `build-push-ecr` job can be dispatched on `main`.

**Causa raiz (histórica):** GitHub Actions `workflow_dispatch` discovery is scoped to the default branch; resolved by the 2026-07-21 merge to `main`.

**Impacto:** The 4 `microservice` Helm releases still aren't installed. What *is* already deployed and verified on the real `fiapx-eks` cluster (2026-07-19 session): `cluster-setup` release (namespace, DB/JWT/New-Relic secrets, DB bootstrap Job - all 4 logical databases created on the shared RDS instance), `metrics-server`, and the `amazon-cloudwatch-observability` addon. That AWS Academy session's credentials have very likely expired (~4h window) and the cluster itself may no longer exist if the lab session ended.

**Pré-requisitos para retomada:** A fresh AWS Academy lab session: refresh the 3 GitHub secrets (`AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`, `AWS_SESSION_TOKEN`), confirm `terraform apply` state still matches reality (re-apply if the cluster was torn down), then `gh workflow run cd.yml`.

**Critério para considerar resolvida:** All 4 ECR repositories (`fiapx/identity-service`, `fiapx/video-service`, `fiapx/processing-worker`, `fiapx/notification-service`) have at least one pushed image tag, and the 4 `microservice` Helm releases install and reach `Ready` on `fiapx-eks`.

### [Epic 010 - CD Pipeline] `deploy` job and E2E smoke test not verified against a real dispatch

**Status (2026-07-21): root cause resolved by merge, execution still pending.** Same update as the Epic 009 entry above - `cd.yml` is dispatchable on `main` now; only a real run with fresh AWS credentials remains.

**Epic/Task afetadas:** Epic 010 (CD Pipeline) - task #22, the `deploy` job and its smoke test in `.github/workflows/cd.yml`.

**Descrição do bloqueio:** The `deploy` job (cluster health check, live AWS CLI env resolution, `helm upgrade --install` of the 4 releases, E2E smoke test) is written and passes static validation (`actionlint`, no findings), but has never actually run in GitHub Actions.

**Causa raiz (histórica):** Same as the Epic 009 entry (workflow_dispatch requires the file on `main`) - resolved.

**Impacto:** No evidence yet that: the live AWS CLI lookups (RDS endpoint by instance identifier, S3 bucket by name prefix) return the expected values in a GitHub Actions runner's environment; `helm upgrade --install` succeeds end-to-end from a clean Actions runner (as opposed to this session's manual run, which already found and fixed one real bug - the `psql`/`PGDATABASE` issue in `cluster-setup`, so the `deploy` job's untested paths carry similar risk); the E2E smoke test's assumptions hold (LoadBalancer hostname becomes available within the 5-minute poll window, the register/login JSON field names match, `ffmpeg` is preinstalled on the `ubuntu-latest` runner image as expected).

**Pré-requisitos para retomada:** A fresh AWS Academy lab session (same as Epic 009 above), then `gh workflow run cd.yml`.

**Critério para considerar resolvida:** A `cd.yml` run completes green end-to-end, including the smoke test asserting `PROCESSED` and a successful download URL.

### [Epic 016 - Observability] New Relic account/License Key needed

**Epic/Task afetadas:** Epic 016 (Observability) - task #23, `fiapx-newrelic-license` Secret value.

**Descrição do bloqueio:** All the code/infra wiring for OTLP export to New Relic is implemented and verified (OTel Java agent in all 4 images, verified inert in `docker-compose-smoke`; Helm's `observability` block, verified rendering correctly; `fiapx-newrelic-license` Secret scaffolded in `cluster-setup`, currently blank). The actual license key requires a New Relic account, which is an external, manual step for the user (cannot be created on their behalf).

**Causa raiz:** External dependency - not something resolvable from within this session.

**Impacto:** Until the Secret is populated, `OTEL_EXPORTER_OTLP_HEADERS` resolves to `api-key=` (empty), so New Relic will reject the OTLP payloads (traces/metrics/logs are attempted but not accepted) - no functional gap in the application itself, but no dashboard/service-map evidence until this is set.

**Pré-requisitos para retomada:** User creates a free New Relic account and obtains a License Key.

**Critério para considerar resolvida:** `kubectl create secret` (or a `cluster-setup` re-install with the real key in its values) updates `fiapx-newrelic-license`, and the New Relic UI shows incoming data from at least one of the 4 services.

### [Epic 014 - Documentation & Test Alignment] LocalStack S3/SNS/SQS integration tests - RESOLVED 2026-07-21

**Resolvido.** PR #17 (merged to `main`) added one LocalStack-backed integration test per service, instantiating adapters/consumers directly against a real LocalStack container: video-service (`S3StorageAdapter`, `SnsEventPublisherAdapter`, `ProcessingResultConsumer`), processing-worker (`S3StorageAdapter`, `SnsEventPublisherAdapter`, `VideoUploadedConsumer`), notification-service (`ProcessingNotificationConsumer` only - no S3/SNS use in that service). Verified green in CI with a real Docker daemon (all 4 `build-test-analyze` jobs + `docker-compose-smoke` passed).

**Bonus find:** the test exposed a real production bug - `processing-worker`'s `S3StorageAdapter.downloadOriginal()` always threw `FileAlreadyExistsException` against real S3 (`Files.createTempFile()` creates the destination file, and `S3Client#getObject(request, Path)` refuses to write to a path that already exists). Fixed in the same PR by deleting the reserved temp file before the download call. Existing unit tests never caught this because they mock `StoragePort`/`S3Client` entirely.

**Critério de resolução:** atendido - each of the 3 services has at least one LocalStack-backed integration test for its AWS adapter(s), verified passing in CI.

### [Epic 013 - Demo Frontend] Not exercised against a live backend

**Epic/Task afetadas:** Epic 013 (Demo Frontend) - `frontend/index.html`.

**Descrição do bloqueio:** The page was verified statically (JS syntax via `node --check`, served correctly by a local HTTP server) but never actually driven through the real login -> upload -> status -> download flow against a running identity-service/video-service, since this session has no Docker/cluster access.

**Causa raiz:** Same underlying constraint as the Epic 009/010 blockers - no local Docker and no deployed stack with real endpoints to point the page at yet (the `main` merge itself is done as of 2026-07-21; what's missing now is a fresh AWS Academy session to actually deploy).

**Impacto:** Field-name/behavior assumptions (e.g. `DownloadUrlResponse.url`, `VideoResponse.downloadAvailable`, CORS headers actually reaching the browser) are verified by reading the real DTOs, but not by an actual browser session.

**Pré-requisitos para retomada:** A running stack (`docker compose up` locally, or the deployed cluster once Epic 009/010 clear) plus `IDENTITY_CORS_ALLOWED_ORIGINS`/`VIDEO_CORS_ALLOWED_ORIGINS` set to the frontend's origin.

**Critério para considerar resolvida:** A manual run through the 4-step flow in a real browser against a real backend completes without console errors.

---

*Recorded during TASK-002.6 (Architecture Readiness Review). Do not act on these items outside of an explicitly approved task.*
