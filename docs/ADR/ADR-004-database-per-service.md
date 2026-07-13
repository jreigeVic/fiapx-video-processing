# ADR-004 - Database per Service

## Status

Approved

## Contexto

O HLD define Database per Service como regra arquitetural. Cada microsservico e proprietario exclusivo dos dados de seu dominio.

## Problema

Compartilhar banco entre servicos cria acoplamento, dificulta evolucao independente e permite que um servico modifique dados de outro dominio.

## Alternativas Avaliadas

| Alternativa | Avaliacao |
|-------------|-----------|
| Banco compartilhado por todos os servicos | Contraria o HLD e aumenta acoplamento. |
| Banco logico separado por servico na mesma instancia RDS | Alinhado ao HLD e adequado ao MVP. |
| Instancia RDS separada por servico | Mantem isolamento, mas aumenta custo e complexidade para o MVP. |

## Decisao

Utilizar banco logico separado por servico em PostgreSQL, preservando ownership de dados por dominio.

## Justificativa

A separacao logica permite demonstrar Database per Service mantendo simplicidade operacional para o MVP.

## Consequencias

- Identity Service utiliza auth_db.
- Video Service utiliza video_db.
- Notification Service utiliza notification_db.
- Processing Worker nao possui ownership sobre o banco do Video Service.
- Nenhum servico pode escrever diretamente no banco de outro servico.

## Trade-offs

| Beneficio | Trade-off |
|-----------|-----------|
| Isolamento de dominio | Mais migracoes Flyway para manter. |
| Evolucao independente | Consultas entre dominios devem ocorrer por APIs ou eventos. |
| Aderencia a microsservicos | Consistencia entre dominios e eventual. |

## Referencias

- docs/HLD/06 - Architecture Overview.md
- docs/HLD/09 - Event-Driven Architecture.md
- .ai/rules/architecture-rules.md
