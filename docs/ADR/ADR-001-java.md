# ADR-001 - Java 21 e Spring Boot 3.x

## Status

Approved

## Contexto

A plataforma FIAP X Video Processing sera composta por microsservicos cloud native. O HLD define Java 21, Spring Boot 3.x, Clean Architecture, Hexagonal Architecture, Spring Security, Spring Data JPA e Flyway como base de implementacao dos servicos.

## Problema

Os microsservicos precisam de uma stack padronizada, madura, testavel e compativel com seguranca, persistencia, observabilidade e execucao em containers.

## Alternativas Avaliadas

| Alternativa | Avaliacao |
|-------------|-----------|
| Java 21 com Spring Boot 3.x | Alinhada ao HLD, madura para microsservicos e compativel com a stack aprovada. |
| Go | Presente apenas no projeto base legado, mas nao faz parte da stack aprovada para a nova arquitetura. |
| Node.js | Nao definido no HLD e nao deve ser introduzido como nova stack. |

## Decisao

Utilizar Java 21 com Spring Boot 3.x como stack de backend dos microsservicos.

## Justificativa

Java 21 e Spring Boot 3.x atendem aos requisitos de manutenibilidade, testabilidade, seguranca e integracao com PostgreSQL, AWS, OpenTelemetry e Kubernetes definidos no HLD.

## Consequencias

- Todos os microsservicos backend devem seguir a mesma stack.
- Controllers devem permanecer sem regra de negocio.
- Casos de uso devem residir na camada Application.
- Regras de negocio devem permanecer isoladas no Domain.
- DTOs devem utilizar records quando apropriado.

## Trade-offs

| Beneficio | Trade-off |
|-----------|-----------|
| Padronizacao tecnica entre servicos | Maior custo inicial que manter o projeto base em Go. |
| Ecossistema maduro para seguranca e persistencia | Exige disciplina na separacao de camadas. |
| Forte suporte a testes e observabilidade | Requer configuracao consistente por servico. |

## Referencias

- docs/HLD/06 - Architecture Overview.md
- docs/HLD/14 - CI-CD.md
- .ai/context/stack.md
- .ai/rules/coding-rules.md
