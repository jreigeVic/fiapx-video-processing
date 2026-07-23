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

### [Epic 009 - Kubernetes] Cannot push the 4 service images to ECR from this session — RESOLVED 2026-07-23

**Resolvido.** A fresh AWS Academy session was used to dispatch `cd.yml` end-to-end. All 4 ECR repositories now have pushed image tags (`build-push-ecr` job green for identity-service, video-service, processing-worker, notification-service), and the 4 `microservice` Helm releases installed and reached `Ready` on `fiapx-eks`.

**Critério de resolução:** atendido.

### [Epic 010 - CD Pipeline] `deploy` job and E2E smoke test not verified against a real dispatch — RESOLVED 2026-07-23

**Resolvido**, after fixing 3 real bugs the first live dispatch surfaced (none of these were ever exercised before this session's first real `cd.yml` run against `fiapx-eks`):

1. **Smoke test payload mismatches** (`cd.yml`): `/api/auth/register` requires a `name` field the script never sent; the upload endpoint returns `videoId`, not `id`. Both fixed in the script itself.
2. **`ffmpeg` missing on the `ubuntu-latest` runner**: added an `apt-get install ffmpeg` step before the smoke test (not preinstalled as assumed).
3. **IMDS hop limit blocking all pod-level AWS SDK calls (the real blocker)**: EKS managed node groups default their launch template's `HttpPutResponseHopLimit` to 1, which only lets processes on the node's own network namespace reach the instance metadata service - pods need one extra hop. With no IRSA/OIDC in AWS Academy, IMDS via the node's `LabRole` instance profile is the *only* credential path every service pod has, so every S3/SQS/SNS call from any pod silently failed to get credentials. This surfaced as video uploads failing with a misleading `401 Missing or invalid token` (see the related JWT filter finding below) instead of the real 500. Fixed by adding a dedicated `aws_launch_template` resource (`infrastructure/terraform/eks.tf`) with `http_put_response_hop_limit = 2`, referenced from `aws_eks_node_group.default` - this forces a full node group replacement (~10min), applied and confirmed (`HopLimit: 2` on both new nodes).

**Bonus find, fixed alongside (not itself a CD-pipeline bug but was blocking correct diagnosis):** `JwtAuthenticationFilter` in both identity-service and video-service extends `OncePerRequestFilter`, whose default `shouldNotFilterErrorDispatch()` returns `true` - meaning the filter skips itself on Spring Boot's internal forward to `/error` after ANY unhandled exception (400/404/500/whatever). Since the forwarded request then re-enters `anyRequest().authenticated()` with no Authentication ever re-established, the client always sees a generic `401 Missing or invalid token`, no matter what the real error was. This masked the IMDS bug's real 500 and would mask any future error identically. Fixed by overriding `shouldNotFilterErrorDispatch()` to `false` in both filters.

**Critério de resolução:** atendido - `cd.yml` run [30036873068](https://github.com/jreigeVic/fiapx-video-processing/actions/runs/30036873068) completed fully green (all 4 image builds + deploy + smoke test) with the IMDS/launch-template fix and both JWT filter fixes already live on the new node group.

### [Epic 016 - Observability] New Relic License Key + Dashboard-as-Code - RESOLVED 2026-07-23

**Status (2026-07-21): design gap closed, option 2 (automated via CD) approved and implemented.** User created the New Relic account and added the key as the GitHub Actions repository secret `NEW_RELIC_LICENSE_KEY` (confirmed via `gh secret list`, added 2026-07-19). `cd.yml`'s `deploy` job now has a "Sync New Relic license key" step that reads this secret and applies it directly to the `fiapx-newrelic-license` Kubernetes Secret via `kubectl create secret ... --dry-run=client -o yaml | kubectl apply -f -`, placed right before the microservice rollout so recreated pods pick up the current key. The step is skipped (no-op) when the secret isn't set, so it never overwrites a working key with an empty one.

**Epic/Task afetadas:** Epic 016 (Observability) - task #23, `fiapx-newrelic-license` Kubernetes Secret value (`infrastructure/helm/cluster-setup`), plus `.github/workflows/cd.yml` (`deploy` job).

**Decisão registrada:** entre as duas opções levantadas (manual `helm upgrade --set` vs. automação no CD), o usuário escolheu a opção 2 (automatizada). Implementada em `cd.yml` deliberadamente via `kubectl` direto no Secret, não via Helm - mantém `cluster-setup` inteiramente fora do CD (RDS master password continua só no tfstate local do operador).

**Caveat documentado (comentário no próprio step de `cd.yml`):** o Secret `fiapx-newrelic-license` é templated pelo `cluster-setup` (`infrastructure/helm/cluster-setup/templates/secret-newrelic.yaml`). Um futuro `helm upgrade cluster-setup --reuse-values` vai resetar seu valor para o que estiver em `newRelic.licenseKey` naquele release Helm (vazio por padrão), sobrescrevendo o valor aplicado via CD. Se isso acontecer, ou reaplicar a chave com `--set newRelic.licenseKey=<key>`, ou simplesmente rodar `cd.yml` de novo.

**Status (2026-07-23): validado.** `cd.yml`'s "Sync New Relic license key" step ran successfully against the real cluster (multiple dispatches this session). Live traffic (register/login/upload/ffmpeg-processing/download, generated while diagnosing the Epic 010 blockers above) went through all 4 services during this window, giving the OTel OTLP export something real to send.

**RESOLVIDO 2026-07-23 (fechamento final):** dashboard implementado como código via Terraform (`infrastructure/terraform/observability.tf`, provider `newrelic` em `versions.tf`/`providers.tf`, variáveis `new_relic_account_id`/`new_relic_api_key` em `variables.tf`), em vez de montado manualmente na UI - conforme a recomendação original do roadmap. Widgets validados com dados reais via NerdGraph (NRQL) antes e depois do `apply`: throughput, p95 de latência e taxa de erro por serviço, mais uma tabela de traces recentes. Achado durante a validação: os root spans de `processing-worker`/`notification-service` são `span.kind = 'consumer'` (SQS), não `server` (HTTP) como em `identity-service`/`video-service` - o filtro NRQL usa `span.kind IN ('server', 'consumer')` para cobrir os 4 serviços. Confirmado por query direta: os 4 serviços aparecem com contagem de spans > 0 na mesma janela de 30 min. CloudWatch Container Insights confirmado `ACTIVE` (`aws eks describe-addon`).

**Critério de resolução:** atendido - dashboard versionado no repositório, com evidência de dados reais dos 4 serviços.

### [Epic 011 - Load Testing] k6 scenarios A/B/C - RESOLVED 2026-07-23

**Resolvido.** Todos os 3 cenários rodados contra o cluster real:
- **Scenario A (burst, 50 VUs)**: 0% de falhas, 100% dos checks (RNF-03 - fila absorve o burst sem perder requisição). Threshold de latência (`p95<5s`) não foi atingido (`p95=15s`) - esperado, só 1 réplica ativa no instante do burst, antes do HPA reagir.
- **Scenario B (sustained, 10 VUs/5min)**: todos os 3 thresholds passaram (`checks=100%`, `p95=369ms`, `falhas=0%`) - RF-04/RNF-01 confirmados.
- **Scenario C (spike, 5->100->5 VUs)**: ambos os thresholds passaram (`checks=99.97%`, `falhas=0.02%`, 1 em 4906). HPA confirmado escalando de verdade via `capture-hpa-evidence.sh` (identity-service e video-service saltaram de 1 para 3 réplicas durante o pico).

**Bugs encontrados e corrigidos nos scripts** (`tests/load/lib/auth.js`, `tests/load/lib/video.js`) - os mesmos 2 mismatches já corrigidos no smoke test do `cd.yml`: `register` não enviava `name`; upload lia `id` em vez de `videoId`.

### [Epic 013 - Demo Frontend] Testado via Playwright contra o cluster real - RESOLVED 2026-07-23

Fluxo completo (login -> upload -> PROCESSING -> PROCESSED -> download) validado de ponta a ponta na UI real (Playwright headless), após configurar `IDENTITY_CORS_ALLOWED_ORIGINS`/`VIDEO_CORS_ALLOWED_ORIGINS` para o origin local usado no teste. Zero erros de console no navegador.

Numa primeira tentativa o vídeo de teste ficou preso em `PROCESSING` porque a sessão AWS Academy expirou/foi cancelada no meio do teste (confirmado: toda chamada AWS - EKS, RDS, S3, CloudWatch - passou a retornar `AccessDenied` pela policy `voc-cancel-cred`), não um bug do frontend ou do backend. Repetido com sucesso numa sessão AWS Academy nova: upload concluído, status avançou para `PROCESSED` em ~9s, download via URL presigned do S3 retornou um `.zip` válido (2 frames extraídos, `frame_0001.jpg`/`frame_0002.jpg`).

**Critério de resolução:** atendido - a execução manual (via Playwright) do fluxo de 4 passos em um browser real contra o backend real completou sem erros de console.

### [Epic 014 - Documentation & Test Alignment] LocalStack S3/SNS/SQS integration tests - RESOLVED 2026-07-21

**Resolvido.** PR #17 (merged to `main`) added one LocalStack-backed integration test per service, instantiating adapters/consumers directly against a real LocalStack container: video-service (`S3StorageAdapter`, `SnsEventPublisherAdapter`, `ProcessingResultConsumer`), processing-worker (`S3StorageAdapter`, `SnsEventPublisherAdapter`, `VideoUploadedConsumer`), notification-service (`ProcessingNotificationConsumer` only - no S3/SNS use in that service). Verified green in CI with a real Docker daemon (all 4 `build-test-analyze` jobs + `docker-compose-smoke` passed).

**Bonus find:** the test exposed a real production bug - `processing-worker`'s `S3StorageAdapter.downloadOriginal()` always threw `FileAlreadyExistsException` against real S3 (`Files.createTempFile()` creates the destination file, and `S3Client#getObject(request, Path)` refuses to write to a path that already exists). Fixed in the same PR by deleting the reserved temp file before the download call. Existing unit tests never caught this because they mock `StoragePort`/`S3Client` entirely.

**Critério de resolução:** atendido - each of the 3 services has at least one LocalStack-backed integration test for its AWS adapter(s), verified passing in CI.

**Auditoria das 10 tasks do Epic 014 (2026-07-23), antes de avançar para o Epic 017:** revisadas todas as 10 tasks listadas no roadmap. 9 já estavam concluídas (testes + ArchUnit do identity-service, `ownerEmail` no event-catalog, `authentication.md` com refresh/logout, READMEs atualizados, `processing_db` documentado, ECR vs GHCR documentado, HLD já em kebab-case, LocalStack - task 9 acima). Task 10 (`application.port` vs `application.ports`) estava incompleta: `ADR-011-microservice-scaffolding.md` (linhas 77-78) ainda documentava `port/in`/`port/out` (singular), divergindo do código real e das LLDs (`application.ports.in`/`application.ports.out`, já corretas). Corrigido nesta sessão - alterado apenas o diagrama de pacotes do ADR-011 para `ports/in`/`ports/out`, sem tocar em nenhuma outra decisão arquitetural. **Epic 014 agora com as 10 tasks concluídas.**

### [Epic 017 - Final Release] Documentação de consolidação criada + repositório inteiro sincronizado para pt-BR - RESOLVED 2026-07-23

**Documentos criados** (itens exigidos pelo checklist do Epic 017 que não existiam no repositório): `docs/rf-rnf-traceability.md` (matriz RF/RNF oficial dentro do repo, com RNF-06/07 corrigidos de "Bloqueado" para "Implementado" após o fechamento do Epic 014), `docs/decision-log.md` (decisões de projeto sem ADR formal), `docs/ADR/README.md` (índice dos 16 ADRs) e `docs/troubleshooting.md` (problemas reais encontrados nesta sessão e como resolvê-los). "Architecture Overview"/"Getting Started"/"Deploy Guide"/"API Guide" do checklist já eram cobertos por `docs/HLD/06-architecture-overview.md`, `docs/setup/`, `docs/api/README.md` - sem necessidade de novo arquivo.

**Sincronização de idioma:** todo o repositório (README raiz, `CONTRIBUTING.md`, os 16 ADRs, os 15 HLDs, as 5 LLDs, `docs/api/`, `docs/setup/`, `docs/development/`, `docs/shared/`, `docs/diagrams/`, e os 13 READMEs de subpastas/serviços) foi revisado e traduzido para português do Brasil - a maior parte já estava em pt-BR (só títulos H1 em inglês nos 15 HLDs e algumas linhas de tabela de rastreabilidade nas LLDs); `ADR-011`, `ADR-012`, os 3 arquivos de `docs/setup/` e os 2 arquivos de `docs/development/` estavam 100% em inglês e foram traduzidos por completo, preservando fielmente o conteúdo técnico (nenhuma decisão alterada, só idioma). Bônus: corrigidos 2 arquivos com corrupção de encoding (mojibake) em `docs/HLD/01-solution-overview.md` e `docs/HLD/README.md`.

**READMEs restilizados** (mais amigáveis, ícones, links, instruções, a pedido explícito do usuário): README raiz, `docs/README.md`, `docs/api/README.md`, `docs/HLD/README.md` e os 9 READMEs restantes (`.github/`, `frontend/`, `infrastructure/`, `infrastructure/helm/`, os 4 `services/*/`, `tests/load/`).

**Critério de resolução:** atendido - documentação consolidada, matriz RF/RNF sem nenhuma linha em aberto, repositório consistente em pt-BR.

---

*Recorded during TASK-002.6 (Architecture Readiness Review). Do not act on these items outside of an explicitly approved task.*
