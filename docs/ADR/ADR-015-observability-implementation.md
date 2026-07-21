# ADR-015 - Implementacao da Observabilidade (ADR-008)

## Status

Approved

## Contexto

ADR-008 decidiu instrumentar os microsservicos com OpenTelemetry, concentrar APM/logs/traces no New Relic e usar CloudWatch para a infraestrutura AWS. ADR-011 (Microservice Scaffolding) deliberadamente adiou essa implementacao ("Placeholder Structure Only, No Exporters Configured"), remetendo para uma tarefa futura.

Esta ADR nao substitui a ADR-008 - registra como a decisao dela foi efetivamente implementada, e formaliza a avaliacao/descarte de Prometheus/Grafana/Micrometer standalone feita neste ciclo de fechamento.

## Problema

Nenhum epic do backlog havia retomado a ADR-008 ate o roadmap final de fechamento. Era preciso decidir: (a) instrumentacao standalone (Prometheus/Grafana/Micrometer) versus o texto original da ADR-008 (OpenTelemetry + New Relic); (b) como dividir responsabilidade entre observabilidade de aplicacao e de infraestrutura sem redundancia.

## Alternativas Avaliadas

| Alternativa | Avaliacao |
|-------------|-----------|
| Prometheus + Grafana + Micrometer standalone | Stack adicional a operar (scrape, storage, dashboards) duplicando o que o New Relic ja oferece pronto; HLD-12 ja lista essas ferramentas como "alternativas arquiteturais viaveis para futuras evolucoes", nao como decisao principal. Descartada para este ciclo. |
| Agente proprietario da New Relic (Java APM agent) | Mais simples de configurar, mas contraria o texto literal da ADR-008 ("instrumentar com OpenTelemetry"). Descartada. |
| Agente Java do OpenTelemetry (auto-instrumentacao) exportando via OTLP para o New Relic + CloudWatch Container Insights para o cluster | Mantem fidelidade a ADR-008, evita redundancia de stacks, usa um exportador padrao de mercado (OTLP) em vez de vendor lock-in no agente. |

## Decisao

- **APM/aplicacao**: agente Java do OpenTelemetry (auto-instrumentacao), embutido nas imagens Docker dos 4 servicos, sempre presente mas inerte por padrao (`OTEL_TRACES_EXPORTER=none` etc. no Dockerfile) - o comportamento em `docker-compose`/local/CI nao muda. O Helm (`infrastructure/helm/microservice`) liga a exportacao via variaveis `OTEL_*` apontando para o endpoint OTLP do New Relic (`https://otlp.nr-data.net:4318`), autenticado com a License Key guardada em `fiapx-newrelic-license` (Secret criado em `infrastructure/helm/cluster-setup`, Epic 009).
- **Infraestrutura/cluster**: addon gerenciado do EKS `amazon-cloudwatch-observability` (`infrastructure/terraform/eks.tf`), cobrindo Container Insights. Sem `service_account_role_arn`: os pods do addon rodam sob o instance profile do node (LabRole), mesmo caminho de credenciais que os pods da aplicacao ja usam - sem IRSA/OIDC (bloqueado no AWS Academy).
- Prometheus/Grafana/Micrometer standalone avaliados e descartados por redundancia frente ao New Relic, dado o escopo do hackathon - registrado tambem em `docs/HLD/12 - Observability.md`.

## Justificativa

Preserva o texto da ADR-008 (OpenTelemetry como instrumentacao, New Relic como APM, CloudWatch como infra) sem introduzir uma segunda stack de observabilidade paralela, e sem exigir IRSA/OIDC, indisponivel no AWS Academy.

## Consequencias

- Rebuild das 4 imagens Docker (agente OTel adicionado) e redeploy via CD (Epic 010) apos esta mudanca.
- Tracing distribuido do fluxo completo (upload -> SNS/SQS -> worker -> SNS/SQS -> notification) passa a ser observavel no service map do New Relic, fechando a lacuna de RNF-13 (rastreabilidade) hoje coberta apenas por `videoId`/`status`.
- Depende de uma conta New Relic (gratuita) e da respectiva License Key - criacao de conta e um passo manual do operador, fora do escopo de automacao.
- Dashboard da New Relic como codigo (provider Terraform `newrelic_one_dashboard`) e recomendado mas opcional; se nao adotado, a evidencia final e via prints/screenshots da UI.

## Trade-offs

| Beneficio | Trade-off |
|-----------|-----------|
| Sem stack de observabilidade duplicada | Dependencia de um servico terceiro (New Relic) para APM |
| Nenhuma mudanca de comportamento fora do Kubernetes (docker-compose/CI inalterados) | Imagens Docker maiores (agente OTel ~20-25MB por imagem) |
| CloudWatch Container Insights sem IRSA | Cobertura de infraestrutura limitada ao que o addon gerenciado expõe |

## Referencias

- docs/ADR/ADR-008-observability.md
- docs/ADR/ADR-011-microservice-scaffolding.md
- docs/HLD/12 - Observability.md
- infrastructure/helm/microservice
- infrastructure/terraform/eks.tf
