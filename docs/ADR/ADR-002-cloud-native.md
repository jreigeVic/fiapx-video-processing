# ADR-002 - Arquitetura Cloud Native

## Status

Approved

## Contexto

O HLD define que a plataforma deve substituir o modelo monolitico por uma solucao cloud native baseada em microsservicos, Kubernetes e servicos gerenciados da AWS.

## Problema

O sistema precisa suportar processamento assincrono, escalabilidade, disponibilidade, resiliencia e evolucao independente dos componentes.

## Alternativas Avaliadas

| Alternativa | Avaliacao |
|-------------|-----------|
| Cloud Native com microsservicos | Alinhada ao HLD e aos requisitos do hackathon. |
| Monolito modular | Reduz complexidade operacional, mas nao atende ao objetivo de microsservicos aprovado. |
| Aplicacao serverless completa | Nao foi definida como arquitetura principal no HLD. |

## Decisao

Adotar arquitetura Cloud Native com microsservicos independentes, containers, Kubernetes e servicos gerenciados da AWS.

## Justificativa

A decisao atende aos atributos de qualidade de escalabilidade, disponibilidade, resiliiencia, observabilidade e evolucao independente definidos no HLD.

## Consequencias

- Cada microsservico possui ciclo de build, deploy e escala independente.
- Configuracoes devem ser externas ao codigo.
- Infraestrutura deve ser descrita como codigo.
- Operacao deve considerar logs, metricas e traces desde o inicio.

## Trade-offs

| Beneficio | Trade-off |
|-----------|-----------|
| Escalabilidade independente | Maior complexidade operacional. |
| Isolamento entre dominios | Maior numero de artefatos para manter. |
| Aderencia a requisitos cloud native | Necessidade de automacao e observabilidade consistentes. |

## Referencias

- docs/HLD/01-solution-overview.md
- docs/HLD/05-quality-attributes.md
- docs/HLD/10-deployment-architecture.md
- docs/HLD/15-decision-summary.md
