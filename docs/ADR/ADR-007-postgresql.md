# ADR-007 - PostgreSQL

## Status

Approved

## Contexto

O HLD define PostgreSQL em Amazon RDS como tecnologia de persistencia dos microsservicos.

## Problema

A plataforma precisa persistir usuarios, videos, status de processamento e dados operacionais por dominio.

## Alternativas Avaliadas

| Alternativa | Avaliacao |
|-------------|-----------|
| PostgreSQL | Alinhado ao HLD e aos requisitos de persistencia. |
| Banco em memoria | Nao atende durabilidade. |
| NoSQL como base principal | Nao definido no HLD e nao deve ser introduzido. |

## Decisao

Utilizar PostgreSQL como banco relacional dos microsservicos, com schemas ou bancos logicos separados por servico.

## Justificativa

PostgreSQL e compativel com Spring Data JPA, Flyway, transacoes e modelagem relacional suficiente para o MVP.

## Consequencias

- Migracoes devem ser gerenciadas por Flyway.
- Cada servico mantem suas proprias tabelas.
- Acesso direto entre bancos de servicos e proibido.
- Scripts de banco pertencem ao respectivo microsservico.

## Trade-offs

| Beneficio | Trade-off |
|-----------|-----------|
| Persistencia madura e relacional | Requer migracoes versionadas por servico. |
| Forte suporte no ecossistema Spring | Modelagem deve respeitar limites de dominio. |
| Operacao gerenciada via RDS | Separacao fisica total pode ficar para evolucao futura. |

## Referencias

- docs/HLD/10-deployment-architecture.md
- docs/HLD/15-decision-summary.md
- .ai/context/stack.md
