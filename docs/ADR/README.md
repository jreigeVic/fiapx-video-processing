# 📐 Architecture Decision Records (ADR)

Registro das decisões arquiteturais da plataforma FIAP X Video Processing. Cada ADR documenta o contexto, a decisão tomada e as consequências - nenhuma decisão consolidada é alterada sem um novo ADR (ou uma emenda explícita a um existente).

Todos os ADRs abaixo estão com status **Aprovado**.

| ADR | Título | Resumo |
|---|---|---|
| [001](ADR-001-java.md) | Java 21 e Spring Boot 3.x | Stack base dos 4 microsserviços: Java 21, Spring Boot 3.x, Clean/Hexagonal Architecture, Spring Security, Spring Data JPA, Flyway. |
| [002](ADR-002-cloud-native.md) | Arquitetura Cloud Native | Substituição do modelo monolítico por microsserviços, Kubernetes e serviços gerenciados da AWS. |
| [003](ADR-003-event-driven.md) | Event Driven Architecture | Processamento pesado de vídeo executado de forma assíncrona via eventos, não síncrona. |
| [004](ADR-004-database-per-service.md) | Database per Service | Cada microsserviço é dono exclusivo dos dados do seu domínio - sem banco compartilhado entre serviços. |
| [005](ADR-005-kubernetes.md) | Kubernetes | Kubernetes como plataforma de orquestração de containers da plataforma. |
| [006](ADR-006-sns-sqs.md) | Amazon SNS e Amazon SQS | SNS para publicação de eventos, SQS para entrega aos consumidores (Competing Consumers). |
| [007](ADR-007-postgresql.md) | PostgreSQL | PostgreSQL em Amazon RDS como tecnologia de persistência dos microsserviços. |
| [008](ADR-008-observability.md) | Observabilidade | OpenTelemetry + New Relic (APM) + CloudWatch (infraestrutura) como estratégia de observabilidade. |
| [009](ADR-009-security.md) | Segurança | JWT, Spring Security, BCrypt, Kubernetes Secrets, IAM mínimo, HTTPS, URLs pré-assinadas para S3. |
| [010](ADR-010-ci-cd.md) | CI/CD | GitHub Actions, Docker, ECR, Kubernetes, JaCoCo, SonarCloud e Trivy como base da pipeline. |
| [011](ADR-011-microservice-scaffolding.md) | Scaffolding de Microsserviços e Convenções de Nomenclatura | Estrutura de repositório (monorepo), organização Gradle, convenção de pacotes (`ports/in`, `ports/out`) e nomenclatura de eventos. |
| [012](ADR-012-naming-conventions.md) | Convenções de Nomenclatura | Convenções de nomenclatura para recursos Kubernetes, bancos de dados, tópicos/filas e branches. |
| [013](ADR-013-identity-refresh-logout-scope.md) | Refresh Token e Logout no Escopo do MVP | Inclusão de refresh token e logout no escopo obrigatório do MVP (não mais opcional). |
| [014](ADR-014-shared-rds-instance.md) | Instância Única de RDS para os Bancos Lógicos | Uma única instância RDS hospedando os 4 bancos lógicos, dada a restrição orçamentária/de permissões do AWS Academy - sem violar Database per Service (isolamento lógico, não físico). |
| [015](ADR-015-observability-implementation.md) | Implementação da Observabilidade (ADR-008) | Como a ADR-008 foi efetivamente implementada: agente OpenTelemetry nos 4 serviços exportando via OTLP para New Relic, dashboard como código (Terraform), CloudWatch Container Insights. |
| [016](ADR-016-aws-academy-ses-verification-constraint.md) | Restrição de Verificação de Identidades SES no AWS Academy | Contorno para a limitação do SES em modo sandbox no AWS Academy (remetente e destinatários precisam ser identidades verificadas). |

---

## ➕ Adicionando um novo ADR

1. Copie o formato de um ADR existente (título, Status, Contexto, Decisão, Consequências, Referências).
2. Use o próximo número sequencial disponível.
3. Adicione uma linha nesta tabela.
4. Nunca altere uma decisão arquitetural consolidada sem um ADR aprovado - conforme `CLAUDE.md`.
