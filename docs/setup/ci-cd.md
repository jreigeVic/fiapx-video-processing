# CI/CD Foundation

This document describes the Continuous Integration pipeline that validates every Pull Request before merge, per ADR-010.

No deployment is performed by this pipeline. CD, Kubernetes deployment, AWS provisioning, Terraform and release automation are explicitly out of scope and left for a future task.

---

## Workflow

**File:** [`.github/workflows/ci.yml`](../../.github/workflows/ci.yml)

**Triggers:**

- `pull_request` targeting `main`.
- `workflow_dispatch` (manual run).

**Strategy:** a single job (`build-test-analyze`) runs as a matrix over the four independent Gradle projects under `services/`:

- `identity-service`
- `notification-service`
- `processing-worker`
- `video-service`

Each matrix run is independent (`fail-fast: false`), matching ADR-010's requirement that each service has an independent build.

### Steps per service

1. **Checkout** the repository.
2. **Set up Java 21 (Temurin)** and **Gradle 8.8**, matching the toolchain declared in each `build.gradle.kts` and `gradle-wrapper.properties`.
3. **Resolve Gradle command** — use `./gradlew` when a service has a committed wrapper, otherwise fall back to the system-installed `gradle` (see decision below).
4. **Build and run unit tests** — `build` task (runs `test` as a dependency).
5. **Generate JaCoCo coverage report** — `jacocoTestReport`, producing `build/reports/jacoco/test/jacocoTestReport.xml`. The report is uploaded as a workflow artifact regardless of build outcome.
6. **SonarCloud analysis** — `sonar` task, only when `SONAR_TOKEN` is configured (see below). This keeps the workflow usable before SonarCloud is wired up, and avoids failing PRs from environments where the secret isn't available.
7. **Build Docker image** — reuses each service's existing `Dockerfile`, tagged `fiapx/<service>:ci`. The image is built locally in the runner only; it is never pushed to any registry (GHCR publishing is CD, out of scope here).
8. **Trivy vulnerability scan (preparation)** — scans the image just built and uploads SARIF results to the repository's Security tab. `exit-code: '0'` means the scan never fails the build; this is scan *preparation*, not an enforced gate. Turning it into a blocking gate is a future decision.

---

## Architectural Decisions

### Sonar host URL is the one Sonar setting that lives in `build.gradle.kts`

`jacoco` and `org.sonarqube` are applied in every service's `build.gradle.kts`. Environment-specific values — token, organization, project key — are still passed from the workflow as `-D` system properties, not hardcoded, per ADR-002's consequence that "configuration must be external to code."

`sonar.host.url` is the one exception, and is now set directly in each `build.gradle.kts`:

```kotlin
sonarqube {
    properties {
        property("sonar.host.url", "https://sonarcloud.io")
    }
}
```

Without an explicit `sonar.host.url`, the Gradle Sonar plugin defaults to a local SonarQube server (`http://localhost:9000`). That default is wrong for this project: ADR-010 commits the platform to **SonarQube Cloud**, not a self-hosted SonarQube server — this is a fixed architectural choice, not a per-environment secret or a value that changes between developer machines, CI, or SonarCloud organizations. Treating it like the other three values (an `-D` flag supplied only by `ci.yml`) meant `gradle sonar` silently pointed at a local server by default for anyone running it outside CI — the opposite of the intended behavior. Hardcoding it in the build file makes SonarCloud the default everywhere the `sonar` task runs, while token/organization/project key still flow in from the environment because those *do* vary.

**Validated:** running `gradle sonar` locally (no `-D` flags, no token) now fails with `You must define the following mandatory properties for '<service>': sonar.organization` instead of attempting to reach `localhost:9000`. `sonar.organization` is a SonarCloud-only requirement the plugin enforces — the Gradle Sonar plugin does not ask for it when targeting a self-hosted SonarQube server — so this error is direct proof the plugin is resolving `sonar.host.url` to SonarCloud by default now, for all four services.

`ci.yml`'s `sonar` step no longer passes `-Dsonar.host.url` (removed as a now-redundant duplicate of the build-file default); it still passes `-Dsonar.token`, `-Dsonar.organization` and `-Dsonar.projectKey`, which remain CI-supplied secrets/variables.

### One SonarCloud project per service

Given each `services/*` directory is an independently built Gradle project (own `settings.gradle.kts`, own `gradlew` where present) rather than a Gradle multi-module build, each service is analyzed as its own SonarCloud project rather than merged into one combined analysis. The project key is derived by convention:

```
<SONAR_PROJECT_KEY>-<service-name>
```

e.g. with `SONAR_PROJECT_KEY=fiapx-video-processing`, the Video Service is analyzed under `fiapx-video-processing-video-service`. All services share the same `SONAR_ORGANIZATION`.

### Prefer `./gradlew`, fall back to a pinned system Gradle

Each step resolves `./gradlew` when present and only falls back to a system-installed `gradle`. `video-service` is currently missing its `gradlew`/`gradlew.bat` launcher and wrapper jar — a pre-existing gap that `ci-scaffold.yml` already fills in by generating and committing wrappers on push. Rather than depend on that separate automation having run, `gradle/actions/setup-gradle` installs Gradle `8.8` as a fallback, matching the version now pinned in every service's `gradle-wrapper.properties` (see below), so the pipeline works identically whether or not a wrapper is present.

### Gradle wrapper version corrected from 9.5.1 to 8.8

Validating the pipeline locally (`./gradlew build`) surfaced a real, pre-existing defect: all four services' `gradle-wrapper.properties` were pinned to Gradle `9.5.1`, which is incompatible with the Spring Boot Gradle plugin `3.3.2` already declared in every `build.gradle.kts` — the `bootJar` task fails outright (`CopyProcessingSpec.getDirMode()`, an API Spring Boot's plugin still expects from the Gradle 8.x line). `ci-scaffold.yml` already declared the project's intended CI Gradle version as `8.4`, confirming `9.5.1` was a mismatch rather than an intentional upgrade. All four `gradle-wrapper.properties` were updated to Gradle `8.8` — the newest 8.x release, fully compatible with both Spring Boot `3.3.2` and JDK 21 — and `ci.yml`'s fallback was aligned to the same version. This was verified by running `build` and `jacocoTestReport` locally for all four services after the change (see Validation Results).

### Missing `io.spring.dependency-management` plugin added to `identity-service` and `video-service`

`notification-service` and `processing-worker` already declared `id("io.spring.dependency-management") version "1.1.0"`; `identity-service` and `video-service` did not. Without it, the Spring Boot BOM is never imported, so the versionless `implementation("org.springframework.boot:...")` dependencies in those two services fail to resolve (`Could not find org.springframework.boot:spring-boot-starter-web:.`, empty version). This was reproduced locally and fixed by adding the same plugin declaration already used by the other two services — a one-line, unambiguous fix required for `compileJava` to succeed at all.

### `.gitignore` fix: `.github/` was being silently ignored

The blanket `.*` rule added to `.gitignore` in a prior task (to hide `.ai`) also matched `.github`, since it starts with a dot — meaning the new `ci.yml`, `PULL_REQUEST_TEMPLATE.md` and `CODEOWNERS` files were invisible to `git status` and could never be committed. Fixed by adding `!.github/` after the blanket rule, the same negation pattern already used for `.env.example`.

### `ci-scaffold.yml` removed (TASK-002.6)

`.github/workflows/ci-scaffold.yml` was a leftover from the TASK-002.1 scaffolding step: it regenerated Gradle wrappers on every push to `services/**` using Gradle 8.4 (inconsistent with the 8.8 pinned everywhere else) via a deprecated action, then auto-committed and pushed the result with `contents: write`. It was never a Pull Request gate and was never referenced by any other documentation. Its only real purpose — keeping the Gradle wrapper present and reproducible — is now handled directly by committing each service's wrapper jar and `gradlew` script (see the wrapper-reproducibility fix below), so the workflow was removed as redundant and risky (an auto-push workflow racing with real PR work).

---

## Known Pre-Existing Defects Surfaced by This Pipeline (Not Fixed)

Running the pipeline locally against a real PostgreSQL 16 instance (the one provisioned in TASK-002.3) surfaced two pre-existing application-level defects, deliberately **not** fixed here — they are outside "CI/CD foundation" scope and require an application-level decision, not a build-tooling one:

- **`video-service` test fails against real PostgreSQL** — its `ApplicationUnitTest` has no `@ActiveProfiles("test")` (unlike `identity-service`, which does and uses an in-memory H2 test profile), so it loads the default `application.yml` datasource and hits a real Flyway/PostgreSQL 16 connection. It fails with `FlywayException: Unsupported Database: PostgreSQL 16.14`, which most likely means the `org.flywaydb:flyway-database-postgresql` module (required by Flyway 10.x for PostgreSQL support) is missing from `video-service`'s dependencies.
- **`notification-service` is missing the PostgreSQL JDBC driver** — it configures a `spring.datasource.url` and enables Flyway in `application.yml`, but its `build.gradle.kts` never declares `org.postgresql:postgresql`. Its test currently passes only because, without a driver, Spring Boot can't build a `DataSource` bean, so Flyway auto-configuration silently never activates — the service cannot actually connect to its database in a real deployment.

Recommend a follow-up task to decide test-database strategy (H2 vs. Testcontainers vs. real Postgres) and to close the missing-dependency gaps.

---

## Required GitHub Secrets

Configured in the GitHub repository settings (`Settings > Secrets and variables > Actions`), not committed to the repository. Names match [`.env.example`](../../.env.example) from `platform-setup.md`:

| Secret | Purpose |
|---|---|
| `SONAR_TOKEN` | Authenticates SonarCloud analysis. When absent, the analysis step is skipped rather than failing the pipeline. |
| `SONAR_ORGANIZATION` | Shared SonarCloud organization key for all four service projects. |
| `SONAR_PROJECT_KEY` | Base project key; combined with the service name per service (see above). |

`GITHUB_TOKEN` is provided automatically by GitHub Actions and requires no manual setup.

No AWS, GHCR or New Relic secrets are required by this workflow — none of its steps use them.

---

## Pull Request Template and CODEOWNERS

- [`.github/PULL_REQUEST_TEMPLATE.md`](../../.github/PULL_REQUEST_TEMPLATE.md) — description, motivation, impact and test evidence fields, matching the PR checklist in `.ai/rules/git-rules.md`.
- [`.github/CODEOWNERS`](../../.github/CODEOWNERS) — currently assigns `@jreigeVic` as the default owner for the whole repository. Update this file as the team grows.

---

## Manual Steps

1. Create a SonarCloud organization and one project per service (or confirm the key convention above matches your SonarCloud setup).
2. Add `SONAR_TOKEN`, `SONAR_ORGANIZATION` and `SONAR_PROJECT_KEY` as repository secrets.
3. Enable branch protection on `main` requiring the `build-test-analyze` matrix checks (and CODEOWNERS review) to pass before merge.
4. Review `.github/CODEOWNERS` and add additional owners as the team grows.

---

## Validation Results

- **Workflow syntax**: `.github/workflows/ci.yml` passes `actionlint` (via the `rhysd/actionlint` Docker image) with zero findings, including expression/context validation (an initial `if: ${{ secrets.SONAR_TOKEN != '' }}` was caught and fixed — `secrets` isn't a valid context in step-level `if:`). `ci-scaffold.yml` was removed in TASK-002.6 (see above) and is no longer part of the pipeline.
- **Build**: `./gradlew build` verified locally, after fixes, for all four services:
  - `identity-service`: `BUILD SUCCESSFUL`.
  - `video-service`: build/compile/`bootJar` succeed; `test` task fails (see Known Pre-Existing Defects).
  - `notification-service`: `BUILD SUCCESSFUL`.
  - `processing-worker`: `BUILD SUCCESSFUL`.
- **JaCoCo**: `jacocoTestReport.xml` generated and verified (non-empty, well-formed) for `identity-service`, `notification-service`, `processing-worker`.
- **SonarCloud configuration**: `org.sonarqube` plugin applied to all four `build.gradle.kts`; workflow step verified to skip cleanly (exit 0) rather than fail when `SONAR_TOKEN` is absent, since no SonarCloud project exists yet for this repository.
- **SonarCloud host targeting**: `gradle sonar` run locally with no `-D` flags for all four services (`video-service` via `gradle -p ../video-service sonar` from `identity-service`'s wrapper, since its own wrapper was intentionally left untouched). Every service failed identically with `You must define the following mandatory properties for '<service>': sonar.organization` — the SonarCloud-only validation the plugin performs, proving `sonar.host.url` now resolves to SonarCloud by default rather than `localhost:9000`.
- **Security scan preparation**: Trivy step configured with `exit-code: '0'` (non-blocking) and SARIF upload to the Security tab; not executed end-to-end locally since it depends on the earlier Docker image build step running inside Actions.

---

## Out of Scope

- Continuous Deployment.
- Kubernetes deployment.
- AWS infrastructure provisioning.
- Terraform.
- Release automation.
- Pushing images to GHCR (build-only, for scanning purposes).
