# ADR-003 - Event Driven Architecture

## Status

Approved

## Contexto

O processamento de videos e uma operacao pesada e demorada. O HLD define que todo processamento pesado deve ocorrer de forma assincrona por meio de eventos.

## Problema

O usuario nao deve aguardar o processamento completo do video durante a requisicao de upload. A plataforma tambem nao pode perder solicitacoes em cenarios de pico.

## Alternativas Avaliadas

| Alternativa | Avaliacao |
|-------------|-----------|
| Processamento sincrono | Simples, mas bloqueia a experiencia do usuario e reduz escalabilidade. |
| Comunicacao direta entre servicos | Aumenta acoplamento e dificulta evolucao independente. |
| Event Driven Architecture | Alinhada ao HLD e aos requisitos de resiliencia, escalabilidade e baixo acoplamento. |

## Decisao

Utilizar Event Driven Architecture para coordenar o ciclo de processamento de videos.

## Justificativa

Eventos desacoplam produtores e consumidores, permitem fan-out, suportam retry e DLQ e preservam o principio de que cada servico altera apenas seus proprios dados.

## Consequencias

- Video Service publica VideoUploaded.
- Processing Worker consome VideoUploaded.
- Processing Worker publica VideoProcessed ou VideoFailed.
- Video Service consome eventos de resultado e atualiza o proprio banco.
- Notification Service consome eventos de resultado para notificar usuarios.

## Trade-offs

| Beneficio | Trade-off |
|-----------|-----------|
| Baixo acoplamento | Consistencia eventual. |
| Processamento resiliente | Maior necessidade de idempotencia. |
| Facilidade para novos consumidores | Contratos de eventos precisam ser bem documentados. |

## Referencias

- docs/HLD/09 - Event-Driven Architecture.md
- docs/HLD/03-functional-requirements.md
- .ai/rules/architecture-rules.md
