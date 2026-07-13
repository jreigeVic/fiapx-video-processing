# ADR-008 - Observabilidade

## Status

Approved

## Contexto

O HLD define OpenTelemetry, New Relic e CloudWatch como estrategia de observabilidade da plataforma.

## Problema

Uma arquitetura distribuida baseada em eventos precisa de visibilidade sobre requisicoes, eventos, filas, erros e consumo de recursos.

## Alternativas Avaliadas

| Alternativa | Avaliacao |
|-------------|-----------|
| Logs locais apenas | Insuficiente para operacao distribuida. |
| Stack de observabilidade autogerenciada | Nao e a estrategia principal definida no HLD. |
| OpenTelemetry com New Relic e CloudWatch | Alinhado ao HLD. |

## Decisao

Instrumentar os microsservicos com OpenTelemetry, concentrar APM, logs, metricas e traces no New Relic e utilizar CloudWatch para infraestrutura AWS.

## Justificativa

A combinacao atende aos pilares de logs, metricas e tracing distribuido definidos no HLD.

## Consequencias

- Todos os servicos devem emitir logs estruturados.
- Eventos publicados e consumidos devem possuir correlacao rastreavel.
- Falhas de processamento e mensagens em DLQ devem ser observaveis.
- Health checks devem estar disponiveis para Kubernetes.

## Trade-offs

| Beneficio | Trade-off |
|-----------|-----------|
| Diagnostico ponta a ponta | Requer padronizacao de correlacao. |
| Visibilidade operacional | Aumenta configuracao por servico. |
| Aderencia cloud native | Requer cuidado para nao registrar dados sensiveis. |

## Referencias

- docs/HLD/12 - Observability.md
- docs/HLD/05 - Quality Attributes.md
- .ai/rules/security-rules.md
