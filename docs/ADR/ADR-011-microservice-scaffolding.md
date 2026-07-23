# ADR-011 - Scaffolding de Microsservicos e Convencoes de Nomenclatura

Data: 2026-07-13
Status: Approved
Contexto: TASK-002 — Scaffolding do Projeto

## Decisoes

### 1. Layout do Repositorio

**Decisao: Monorepo**

Um unico repositorio contem todos os microsservicos sob o diretorio `services/`:
- services/
  - identity-service/
  - video-service/
  - processing-worker/
  - notification-service/

Esta abordagem permite:
- Templates e convencoes consistentes entre servicos
- Pipeline de CI/CD unificado
- Gerenciamento simplificado de ferramentas e dependencias
- Mantem a capacidade de deploy independente

Alternativa avaliada: Repositorios separados por servico. Rejeitada para a fase inicial de desenvolvimento devido ao aumento de complexidade.

### 2. Organizacao do Gradle

**Decisao: Projetos Gradle Independentes**

Cada microsservico contem:
- `settings.gradle.kts` independente
- `build.gradle.kts` independente
- Gradle Wrapper independente (`gradle/wrapper/`, `gradlew.bat`)
- Ciclo de vida de build independente

Esta abordagem garante:
- Autonomia completa para cada servico
- Migracao futura simplificada para repositorios separados
- Nenhuma dependencia de configuracao de build compartilhada
- Gerenciamento de versao independente

Alternativa avaliada: Build Gradle multi-projeto com gerenciamento central. Rejeitada para preservar a independencia dos microsservicos.

### 3. Ferramenta de Build: Gradle Kotlin DSL

**Decisao: Usar Gradle com Kotlin DSL (.kts)**

Todos os arquivos de build Gradle usam `build.gradle.kts` e `settings.gradle.kts` com Kotlin DSL.

Justificativa:
- Configuracao Gradle type-safe
- Suporte superior de IDE no IntelliJ IDEA
- Abordagem moderna e recomendada
- Melhor legibilidade e manutenibilidade

### 4. Convencao de Nomenclatura de Pacotes

**Decisao: Usar `com.fiapx.<service>` como pacote raiz**

Cada microsservico adota a seguinte estrutura de pacotes:

```
com.fiapx.identity          (Identity Service)
com.fiapx.video              (Video Service)
com.fiapx.processing         (Processing Worker)
com.fiapx.notification       (Notification Service)
```

Os subpacotes seguem a arquitetura em camadas:

```
com.fiapx.<service>
  ├── application/
  │   ├── usecase/
  │   ├── ports/in/
  │   ├── ports/out/
  │   └── dto/
  ├── domain/
  │   ├── model/
  │   ├── valueobject/
  │   ├── service/
  │   └── exception/
  ├── infrastructure/
  │   ├── adapter/in/web/          (HTTP controllers)
  │   ├── adapter/in/messaging/    (Event consumers)
  │   ├── adapter/out/persistence/ (JPA repositories)
  │   ├── adapter/out/storage/     (S3, external storage)
  │   ├── adapter/out/messaging/   (SNS, SQS publishers)
  │   └── config/
  ├── api/
  │   ├── controller/
  │   ├── request/
  │   ├── response/
  │   └── mapper/
  └── shared/
      ├── error/
      └── observability/
```

Justificativa:
- Propriedade clara do dominio por pacote
- Segue os principios da Clean Architecture
- Alinhado com os padroes da Hexagonal Architecture
- Permite evolucao independente dos pacotes

### 5. Convencao de Nomenclatura de Eventos

**Decisao: Usar PascalCase para Nomes de Eventos**

Todos os eventos seguem a convencao de nomenclatura PascalCase:

```
VideoUploaded    (Event published when video is uploaded)
VideoProcessed   (Event published when processing succeeds)
VideoFailed      (Event published when processing fails)
```

Regras:
- Sem sufixos de versao nos nomes de eventos (ex.: `VideoUploadedV1` proibido)
- Sem snake_case ou kebab-case
- Sem underscores ou caracteres especiais
- Eventos futuros devem seguir a mesma convencao

Justificativa:
- Consistente com as convencoes de nomenclatura de classes Java
- Significado semantico claro no codigo
- Evita versionamento duplicado nos nomes de eventos
- Alinhado com as praticas de domain-driven design

### 6. Configuracao do Docker

**Decisao: Dockerfile Classico com Fat JAR**

Cada microsservico inclui:
- `Dockerfile` usando a imagem base Eclipse Temurin 21 LTS
- Empacotamento Fat JAR (um unico JAR executavel)
- Base Alpine Linux para tamanho minimo de imagem
- `.dockerignore` para excluir arquivos desnecessarios

Template de Dockerfile:
```dockerfile
FROM eclipse-temurin:21-jre-alpine
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar
EXPOSE <port>
ENTRYPOINT ["java","-jar","/app.jar"]
```

Justificativa:
- Deploy simples e previsivel
- Tamanho minimo de imagem com Alpine
- Imagem oficial Eclipse Temurin para Java 21
- Adiado para tarefas futuras: imagens em camadas, ajuste de JVM

### 7. Versao do Java e Toolchain

**Decisao: Java 21 com Toolchain Gradle Explicito**

Todos os servicos declaram explicitamente o toolchain Java 21:

```kotlin
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}
```

Justificativa:
- Java 21 e a versao LTS mais recente
- Toolchain explicito garante builds consistentes
- Gradle baixa automaticamente o JDK correto quando nao encontrado
- Preparado para suporte de longo prazo

### 8. Framework de Testes

**Decisao: JUnit 5 + Mockito + Spring Boot Test**

Todos os testes unitarios e de integracao usam:
- JUnit 5 (Jupiter) como test engine
- Mockito para mocking
- Spring Boot Test starter para testes de integracao

Justificativa:
- Padrao moderno para testes em Java
- Excelente integracao com Spring Boot
- Capacidades abrangentes de assertion e mocking
- Ampla adocao pela industria

### 9. Migracoes de Banco de Dados

**Decisao: Flyway com Migracao Inicial Placeholder**

Cada microsservico inclui:
- Plugin Flyway Maven em `build.gradle.kts`
- `src/main/resources/db/migration/V000__init.sql` (migracao apenas com comentario)
- Configuracao do Flyway em `application.yml`

Justificativa:
- Garante que o Flyway esta configurado corretamente
- Pronto para implementacao do schema de negocio
- Migracao placeholder evita erros do Flyway
- Principio de Database per Service preservado

### 10. Gerenciamento de Configuracao

**Decisao: Spring Boot application.yml com Perfis Especificos por Ambiente**

Cada servico inclui quatro perfis de configuracao:

```
application.yml       (Base configuration)
application-local.yml (Local development)
application-dev.yml   (Development environment)
application-test.yml  (Test environment)
```

Justificativa:
- Convencoes padrao do Spring Boot
- Isolamento de propriedades especificas por ambiente
- Nenhuma credencial de producao no codigo
- Ativacao facil de perfil por alvo de deploy

### 11. Preparacao para Observabilidade

**Decisao: Apenas Estrutura Placeholder, Sem Exporters Configurados**

Cada microsservico inclui:
- Estrutura de projeto pronta para OpenTelemetry
- Nenhuma configuracao de agente New Relic
- Nenhuma implementacao de exporter
- Adiado para tarefas futuras de implementacao

Justificativa:
- Preocupacoes de infraestrutura adiadas para ops
- Evitar credenciais/configuracao prematuras
- Implementacao pertence a tarefas posteriores
- Padroes de observabilidade documentados no HLD 12

### 12. Configuracao de Logging

**Decisao: Logging Padrao do Spring Boot (Logback)**

Cada servico usa:
- Configuracao de logging padrao do Spring Boot
- Logback padrao sem formatacao JSON
- Logging estruturado adiado para tarefas futuras

Justificativa:
- Configuracao minima para a fase de scaffolding
- Pronto para adicao futura de logging estruturado
- Nenhuma dependencia de framework de logging customizado
- Segue o principio de dependencias minimas

### 13. Estilo de Implementacao de DTO

**Decisao: Java Records para DTOs Imutaveis**

Quando apropriado, usar `record` do Java para DTOs de request/response:

```java
public record LoginRequest(String email, String password) {}
public record LoginResponse(String accessToken, String tokenType, Long expiresIn) {}
```

Justificativa:
- Estruturas de dados imutaveis e concisas
- equals/hashCode/toString automaticos
- Reducao de codigo boilerplate
- Adocao de recurso de linguagem do Java 16+

### 14. Bibliotecas Compartilhadas

**Decisao: Sem Modulos Compartilhados**

Cada microsservico permanece completamente autonomo:
- Nenhum modulo pai compartilhado
- Nenhuma biblioteca comum
- Duplicacao minima de codigo aceita em favor da independencia
- Utilitarios de teste duplicados por servico conforme necessario

Justificativa:
- Preserva a independencia dos microsservicos
- Evita acoplamento implicito
- Simplifica o deploy independente
- Reduz a infraestrutura de testes compartilhada

Alternativa avaliada: Criar modulo `services/shared`. Rejeitada para manter a autonomia completa dos servicos.

### 15. Implementacoes de Controller

**Decisao: Scaffold de Classes Controller Sem Endpoints**

Cada servico inclui:
- Classes controller anotadas com `@RestController`
- Nenhum endpoint real (`@GetMapping`, `@PostMapping`, etc.)
- Nenhuma resposta mock ou endpoint de demonstracao
- Logica de negocio adiada para tarefas de implementacao

Justificativa:
- Atende aos requisitos de organizacao de pacotes
- Evita exposicao prematura de endpoints
- Implementacao de negocio adiada explicitamente
- Pronto para adicao incremental de endpoints

## Rastreabilidade

| Referencia | Documento | Requisito |
|-----------|----------|-------------|
| HLD 06 | Architecture Overview | Microsservicos, Clean Architecture, Hexagonal Architecture |
| ADR-001 | Decisao sobre Java 21 | Versao de JDK obrigatoria |
| ADR-005 | Kubernetes | Deployments prontos para container |
| LLD Shared | Shared Architecture | Organizacao de pacotes, camadas |
| TASK-002 | Project Scaffolding | Regras de geracao do scaffold |

## Notas de Implementacao

Este ADR se aplica aos quatro microsservicos:
1. Identity Service (`com.fiapx.identity`)
2. Video Service (`com.fiapx.video`)
3. Processing Worker (`com.fiapx.processing`)
4. Notification Service (`com.fiapx.notification`)

Todos os microsservicos futuros devem adotar estas mesmas convencoes.

## Referencia de Template

O scaffold gerado serve como template de referencia para futuros microsservicos. A promocao para templates reutilizaveis em `.ai/templates/microservice/` requer aprovacao explicita do Software Architect.

## Restricoes

Nenhuma decisao arquitetural pode introduzir:
- Bibliotecas compartilhadas adicionais sem aprovacao
- Esquemas alternativos de nomenclatura de pacotes
- Frameworks de teste diferentes por servico
- Abordagens de configuracao fora do padrao
- Alternativas de ferramenta de build sem emenda ao ADR

Todas as restricoes sao preservadas dos ADRs aprovados (001-010).

### 16. Versionamento do AWS SDK

**Decisao: Usar AWS SDK for Java v2 (BOM)**

Todas as integracoes AWS no projeto DEVEM usar o AWS SDK for Java v2 BOM nos builds Gradle. O BOM deve ser declarado como:

```
implementation(platform("software.amazon.awssdk:bom:2.20.0"))
```

e modulos especificos por servico incluidos conforme necessario, por exemplo:

- `software.amazon.awssdk:s3`
- `software.amazon.awssdk:sns`
- `software.amazon.awssdk:sqs`

Justificativa:
- O SDK v2 fornece clientes nao bloqueantes e configuracao aprimorada
- Um unico BOM garante versoes consistentes entre os servicos

Rastreabilidade:
- Aprovado em revisao do Software Architect durante a TASK-002.1

