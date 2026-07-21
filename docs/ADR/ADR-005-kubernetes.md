# ADR-005 - Kubernetes

## Status

Approved

## Contexto

O HLD define Kubernetes como plataforma de orquestracao dos containers da FIAP X Video Processing Platform.

## Problema

Os microsservicos precisam ser executados, escalados, atualizados e recuperados de falhas de maneira padronizada e automatizada.

## Alternativas Avaliadas

| Alternativa | Avaliacao |
|-------------|-----------|
| Execucao manual em maquinas virtuais | Nao atende a automacao e escalabilidade esperadas. |
| Docker Compose | Util para desenvolvimento local, mas insuficiente como estrategia cloud native principal. |
| Kubernetes | Alinhado ao HLD e aos requisitos de escalabilidade, disponibilidade e entrega continua. |

## Decisao

Utilizar Kubernetes para orquestrar os microsservicos e workloads da plataforma.

## Justificativa

Kubernetes fornece Deployments, Services, ConfigMaps, Secrets, probes, rolling updates, HPA e base para evolucao com KEDA.

## Consequencias

- Cada servico deve possuir imagem Docker propria.
- Configuracoes devem ser externalizadas via ConfigMaps e Secrets.
- Health checks devem ser definidos por servico.
- Escalabilidade inicial utiliza HPA; KEDA permanece evolucao prevista para filas.

## Trade-offs

| Beneficio | Trade-off |
|-----------|-----------|
| Escala horizontal e auto recovery | Maior curva operacional. |
| Deploy independente | Necessidade de manifests ou charts por servico. |
| Padronizacao cloud native | Exige observabilidade e configuracao corretas. |

## Referencias

- docs/HLD/10-deployment-architecture.md
- docs/HLD/13-scalability.md
- .ai/context/architecture.md
