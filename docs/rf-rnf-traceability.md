# ✅ Rastreabilidade RF/RNF × Evidência

Matriz final de fechamento do projeto (Epic 017). Numeração oficial do hackathon - não confundir com a numeração interna de `docs/HLD/03-functional-requirements.md` e `docs/HLD/04-non-functional-requirements.md`, que usam outro agrupamento (conteúdo equivalente, números diferentes).

Nenhuma linha abaixo está em aberto.

## Requisitos Funcionais (RF)

| # | Requisito | Evidência objetiva | Status |
|---|---|---|---|
| RF-01 | Upload de vídeo | `video-service` - `POST /api/videos` ([docs/api/video.md](api/video.md)) | ✅ Implementado |
| RF-02 | Processar e gerar `.zip` de frames | `processing-worker` - `FfmpegVideoProcessorAdapter` | ✅ Implementado |
| RF-03 | Download do `.zip` | `video-service` - `GET /api/videos/{id}/download` (URL pré-assinada S3) | ✅ Implementado |
| RF-04 | Processar +1 vídeo simultaneamente | k6 Cenário B (sustentado, 10 VUs/5min) + Competing Consumers/HPA - ver [`tests/load/README.md`](../tests/load/README.md) | ✅ Implementado e evidenciado |
| RF-05 | Autenticação login/senha | `identity-service` - `POST /api/auth/login` (JWT) ([docs/api/authentication.md](api/authentication.md)) | ✅ Implementado |
| RF-06 | Listagem com status | `video-service` - `GET /api/videos` | ✅ Implementado |
| RF-07 | Notificação de erro/conclusão | `notification-service` - Amazon SES | ✅ Implementado |

## Requisitos Não Funcionais (RNF)

| # | Requisito | Evidência objetiva | Status |
|---|---|---|---|
| RNF-01 | Concorrência multi-vídeo | k6 Cenário B: 100% dos checks, `p95=369ms`, 0% de falhas | ✅ Implementado e evidenciado |
| RNF-02 | Escalabilidade horizontal | HPA + k6 Cenário C (spike 5→100→5 VUs): `identity-service`/`video-service` escalaram de 1→3 réplicas (evidência capturada via `kubectl get hpa`/`top pods`) | ✅ Implementado e evidenciado |
| RNF-03 | Não perder requisições em pico | SQS + DLQ + k6 Cenário A (burst, 50 uploads simultâneos): 0% de falhas, 100% dos checks | ✅ Implementado e evidenciado |
| RNF-04 | Persistir estado das requisições | Tabelas `videos` / `processed_events` / `notifications` (um banco lógico por serviço) | ✅ Implementado |
| RNF-05 | Autenticação login/senha | JWT + Spring Security + BCrypt | ✅ Implementado |
| RNF-06 | Testes automatizados | Os 4 serviços têm suíte de testes (unitários + integração); `identity-service` (antes o único sem cobertura) recebeu testes completos + `HexagonalArchitectureTest` | ✅ Implementado |
| RNF-07 | Boas práticas de arquitetura | `HexagonalArchitectureTest` (ArchUnit) presente nos 4 serviços | ✅ Implementado |
| RNF-08 | Pipeline CI/CD | CI (`.github/workflows/ci.yml`) + CD (`.github/workflows/cd.yml`), ambos validados verdes contra o cluster real | ✅ Implementado |
| RNF-09 | Código no GitHub | `github.com/jreigeVic/fiapx-video-processing` | ✅ Implementado |
| RNF-10 | Processamento assíncrono | `VideoUploaded` → SQS → `processing-worker` | ✅ Implementado |
| RNF-11 | Desacoplar via mensageria | SNS + SQS + DLQ | ✅ Implementado |
| RNF-12 | Resiliência a falhas | Idempotência + evento `VideoFailed` + retry/DLQ | ✅ Implementado |
| RNF-13 | Rastreabilidade por ID/status | `videoId`/`status` + tracing distribuído via OpenTelemetry → New Relic (dashboard versionado em `infrastructure/terraform/observability.tf`) | ✅ Implementado |

## Notas

- Todas as evidências de carga (k6) estão documentadas com números reais em [`tests/load/README.md`](../tests/load/README.md).
- A evidência de escalabilidade (HPA) foi capturada ao vivo contra o cluster EKS real durante o Cenário C, não apenas configurada.
- RNF-13 (tracing distribuído) tem evidência visual no dashboard New Relic (`terraform output new_relic_dashboard_url`), com dados reais dos 4 serviços confirmados via NRQL antes do fechamento.
- Decisões que sustentam esta matriz (ex.: por que não há paralelismo interno no consumer, por que HPA por CPU e não KEDA) estão registradas em [`decision-log.md`](decision-log.md).
