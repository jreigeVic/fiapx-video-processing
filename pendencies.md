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

*Recorded during TASK-002.6 (Architecture Readiness Review). Do not act on these items outside of an explicitly approved task.*
