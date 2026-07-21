# ADR-006 - Amazon SNS e Amazon SQS

## Status

Approved

## Contexto

O HLD define Amazon SNS para publicacao de eventos e Amazon SQS para entrega aos consumidores.

## Problema

A plataforma precisa distribuir eventos para um ou mais consumidores, suportar picos e evitar perda de solicitacoes durante processamento assincrono.

## Alternativas Avaliadas

| Alternativa | Avaliacao |
|-------------|-----------|
| SQS direto sem SNS | Atende uma fila, mas limita fan-out futuro. |
| Broker autogerenciado | Aumenta esforco operacional e nao privilegia servicos gerenciados AWS. |
| SNS com SQS | Alinhado ao HLD, suporta pub/sub, filas por consumidor e DLQ. |

## Decisao

Utilizar Amazon SNS como broker de eventos e Amazon SQS como filas de consumo dos microsservicos.

## Justificativa

SNS/SQS preserva baixo acoplamento, permite fan-out, retry, DLQ e consumo independente por servico.

## Consequencias

- Eventos sao publicados em topicos SNS.
- Cada consumidor recebe eventos por fila SQS propria.
- Processing Worker consome eventos pela fila de processamento.
- Video Service e Notification Service consomem eventos de resultado por filas proprias.
- Mensagens nao processadas devem ir para DLQ.

## Trade-offs

| Beneficio | Trade-off |
|-----------|-----------|
| Servico gerenciado e resiliente | Requer idempotencia no consumidor. |
| Fan-out nativo | Contratos de evento precisam ser versionados no futuro. |
| Escalabilidade de consumidores | Observabilidade de filas passa a ser obrigatoria. |

## Referencias

- docs/HLD/09-event-driven-architecture.md
- docs/HLD/13-scalability.md
- .ai/context/source/archfiapx.md
