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

## ADR Numbering

`docs/ADR/ADR-011-microservice-scaffolding.md` and `docs/ADR/ADR-011-naming-conventions.md` both claim ADR number 011:

- `docs/shared/package-conventions.md` says "refer to ADR-011" without disambiguating which of the two it means.
- `ADR-011-naming-conventions.md` has an unfilled `Date: YYYY-MM-DD` placeholder.
- `ADR-011-naming-conventions.md`'s Database Naming section uses `identity_db` as its worked example, while every other artifact (`ADR-004-database-per-service.md`, LLDs, diagrams, and the actual `application.yml`/init script) uses `auth_db`. `docs/setup/local-development.md` already flags this discrepancy and defers reconciliation.
- `ADR-011-naming-conventions.md` lists example events (`UserCreated`, `UserAuthenticated`, `NotificationSent`) that appear nowhere else in the docs tree and contradict `docs/LLD/identity-service.md`, which states Identity Service publishes no events.

**Resolution needed**: renumber one document (e.g. `ADR-011-naming-conventions.md` → `ADR-012`), fill in the missing date, and reconcile the `identity_db`/`auth_db` example.

---

## Service Dependencies

### Notification Service

`docs/LLD/notification-service.md` mandates a `JpaNotificationRepositoryAdapter` and a `notification_db` (with a full ER model for `notifications` and `processed_events`), but `services/notification-service/build.gradle.kts` has `flyway-core` with no `spring-boot-starter-data-jpa` and no `org.postgresql:postgresql` driver. The build cannot currently support the persistence layer its own LLD requires.

### Video Service

`docs/LLD/video-service.md` explicitly requires token validation (401 on missing/invalid token) and lists "Spring Security" as a dependency, but `services/video-service/build.gradle.kts` has neither `spring-boot-starter-security` nor a JWT library (`identity-service` has both `spring-boot-starter-security` and `jjwt`; `video-service` has neither).

Separately, and already tracked in `docs/setup/ci-cd.md` ("Known Pre-Existing Defects Surfaced by This Pipeline"): `video-service`'s `ApplicationUnitTest` has no `@ActiveProfiles("test")`, so it loads the real `application.yml` datasource against PostgreSQL 16 and fails with `FlywayException: Unsupported Database: PostgreSQL 16.14` — most likely because `org.flywaydb:flyway-database-postgresql` (required by Flyway 10.x for PostgreSQL support) is missing from its dependencies. Reconfirmed during TASK-002.6 build validation.

### Processing Worker

`services/processing-worker/build.gradle.kts` has `flyway-core` with no JPA/Postgres driver. Lower risk than the Notification Service gap, since `docs/LLD/processing-worker.md` treats local persistence as optional ("caso utilize persistencia local para idempotencia").

**Resolution needed**: add the missing dependencies to each service's `build.gradle.kts` when that service's implementation task starts.

---

*Recorded during TASK-002.6 (Architecture Readiness Review). Do not act on these items outside of an explicitly approved task.*
