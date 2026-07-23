# ⚙️ GitHub Workflows

Automação de CI/CD e configuração de colaboração do repositório.

## 📂 O que tem aqui

| Arquivo | Para que serve |
|---|---|
| [`workflows/ci.yml`](workflows/ci.yml) | Pipeline de CI (roda em todo Pull Request para `main`) |
| [`workflows/cd.yml`](workflows/cd.yml) | Pipeline de CD (deploy manual na AWS) |
| [`CODEOWNERS`](CODEOWNERS) | Define quem precisa aprovar Pull Requests |
| [`PULL_REQUEST_TEMPLATE.md`](PULL_REQUEST_TEMPLATE.md) | Checklist padrão exigido em todo PR |

## ✅ CI (`ci.yml`)

Roda em todo Pull Request e push para `main`. Principais etapas:

- **Lint do contrato OpenAPI** (`docs/api/openapi.yaml`) com Redocly.
- **Validação do Terraform** (`fmt`, `init -backend=false`, `validate`).
- **Lint dos charts Helm** (`microservice`, `cluster-setup`) + render com os `values` reais de cada serviço.
- **Build, testes e análise por serviço** (`identity-service`, `video-service`, `processing-worker`, `notification-service`): build + testes (Gradle), ArchUnit, CodeQL, cobertura JaCoCo, relatório de dependências desatualizadas, OWASP Dependency-Check (só roda se `NVD_API_KEY` estiver configurada), SonarCloud (só roda se os secrets estiverem configurados) e build da imagem Docker + scan Trivy.
- **Smoke test com docker compose**: sobe a stack completa (Postgres, LocalStack e os 4 serviços) e aguarda todos os healthchecks ficarem `healthy`.

## 🚀 CD (`cd.yml`)

Disparo **manual apenas** (`workflow_dispatch`) - as credenciais da sessão AWS Academy expiram a cada ~4h, então não há identidade AWS de longa duração para disparar automaticamente. Dois jobs:

1. **`build-push-ecr`**: builda o jar de cada serviço e publica a imagem no ECR (tag pela SHA do commit e `latest`).
2. **`deploy`**: resolve o ambiente via AWS CLI, faz `helm upgrade --install` das 4 releases e roda um smoke test end-to-end (registro → login → upload → status `PROCESSED` → download).

Runbook completo (secrets necessários, ordem de execução, procedimento por sessão de laboratório) está em 📖 [`infrastructure/helm/README.md`](../infrastructure/helm/README.md#-cd-github-actions).

## 🔎 Documentação relacionada

- [`docs/HLD/14-ci-cd.md`](../docs/HLD/14-ci-cd.md) - visão arquitetural do pipeline
- [`docs/api/README.md`](../docs/api/README.md) - contrato OpenAPI validado pelo CI
- [`infrastructure/helm/README.md`](../infrastructure/helm/README.md) - deploy via Helm e runbook de CD
