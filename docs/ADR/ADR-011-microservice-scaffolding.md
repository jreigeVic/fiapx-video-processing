# ADR-011: Microservice Scaffolding and Naming Conventions

Date: 2026-07-13
Status: Approved
Context: TASK-002 — Project Scaffolding

## Decisions

### 1. Repository Layout

**Decision: Monorepo**

A single repository contains all microservices under the `services/` directory:
- services/
  - identity-service/
  - video-service/
  - processing-worker/
  - notification-service/

This approach enables:
- Consistent cross-service templates and conventions
- Unified CI/CD pipeline
- Simplified tooling and dependency management
- Maintains independent deployability

Alternative considered: Separate repositories per service. Rejected for initial development phase due to increased complexity.

### 2. Gradle Organization

**Decision: Independent Gradle Projects**

Each microservice contains:
- Independent `settings.gradle.kts`
- Independent `build.gradle.kts`
- Independent Gradle Wrapper (`gradle/wrapper/`, `gradlew.bat`)
- Independent build lifecycle

This approach ensures:
- Complete autonomy for each service
- Simplified future migration to separate repositories
- No shared build configuration dependencies
- Independent version management

Alternative considered: Multi-project Gradle build with central management. Rejected to preserve microservice independence.

### 3. Build Tool: Gradle Kotlin DSL

**Decision: Use Gradle with Kotlin DSL (.kts)**

All Gradle build files use `build.gradle.kts` and `settings.gradle.kts` with Kotlin DSL.

Rationale:
- Type-safe Gradle configuration
- Superior IDE support in IntelliJ IDEA
- Modern and recommended approach
- Better readability and maintainability

### 4. Package Naming Convention

**Decision: Use `com.fiapx.<service>` as root package**

Each microservice adopts the following package structure:

```
com.fiapx.identity          (Identity Service)
com.fiapx.video              (Video Service)
com.fiapx.processing         (Processing Worker)
com.fiapx.notification       (Notification Service)
```

Sub-packages follow the layered architecture:

```
com.fiapx.<service>
  ├── application/
  │   ├── usecase/
  │   ├── port/in/
  │   ├── port/out/
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

Rationale:
- Clear domain ownership per package
- Follows Clean Architecture principles
- Aligns with Hexagonal Architecture patterns
- Enables independent package evolution

### 5. Event Naming Convention

**Decision: Use PascalCase for Event Names**

All events follow PascalCase naming convention:

```
VideoUploaded    (Event published when video is uploaded)
VideoProcessed   (Event published when processing succeeds)
VideoFailed      (Event published when processing fails)
```

Rules:
- No version suffixes in event names (e.g., `VideoUploadedV1` forbidden)
- No snake_case or kebab-case
- No underscores or special characters
- Future events must follow the same convention

Rationale:
- Consistent with Java class naming conventions
- Clear semantic meaning in code
- Prevents duplicate versioning in event names
- Aligns with domain-driven design practices

### 6. Docker Configuration

**Decision: Classic Dockerfile with Fat JAR**

Each microservice includes:
- `Dockerfile` using Eclipse Temurin 21 LTS base image
- Fat JAR packaging (single executable JAR)
- Alpine Linux base for minimal image size
- `.dockerignore` to exclude unnecessary files

Dockerfile Template:
```dockerfile
FROM eclipse-temurin:21-jre-alpine
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar
EXPOSE <port>
ENTRYPOINT ["java","-jar","/app.jar"]
```

Rationale:
- Simple and predictable deployment
- Minimal image size with Alpine
- Official Eclipse Temurin image for Java 21
- Deferred to future tasks: layered images, JVM tuning

### 7. Java Version and Toolchain

**Decision: Java 21 with Explicit Gradle Toolchain**

All services explicitly declare Java 21 toolchain:

```kotlin
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}
```

Rationale:
- Java 21 is the latest LTS release
- Explicit toolchain ensures consistent builds
- Gradle automatically downloads correct JDK if not found
- Future-proof for long-term support

### 8. Testing Framework

**Decision: JUnit 5 + Mockito + Spring Boot Test**

All unit and integration tests use:
- JUnit 5 (Jupiter) as test engine
- Mockito for mocking
- Spring Boot Test starter for integration tests

Rationale:
- Modern standard for Java testing
- Excellent Spring Boot integration
- Comprehensive assertion and mocking capabilities
- Wide industry adoption

### 9. Database Migrations

**Decision: Flyway with Placeholder Initial Migration**

Each microservice includes:
- Flyway Maven plugin in `build.gradle.kts`
- `src/main/resources/db/migration/V000__init.sql` (comment-only migration)
- Flyway configuration in `application.yml`

Rationale:
- Ensures Flyway is properly configured
- Ready for business schema implementation
- Placeholder migration prevents Flyway errors
- Database per service principle preserved

### 10. Configuration Management

**Decision: Spring Boot application.yml with Environment-Specific Profiles**

Each service includes four configuration profiles:

```
application.yml       (Base configuration)
application-local.yml (Local development)
application-dev.yml   (Development environment)
application-test.yml  (Test environment)
```

Rationale:
- Standard Spring Boot conventions
- Environment-specific properties isolation
- No production credentials in code
- Easy profile activation per deployment target

### 11. Observability Preparation

**Decision: Placeholder Structure Only, No Exporters Configured**

Each microservice includes:
- Project structure ready for OpenTelemetry
- No New Relic agent configuration
- No exporter implementations
- Deferred to future implementation tasks

Rationale:
- Infrastructure concerns deferred to ops
- Avoid premature credential/configuration
- Implementation belongs in later tasks
- Observability patterns documented in HLD 12

### 12. Logging Configuration

**Decision: Spring Boot Default Logging (Logback)**

Each service uses:
- Default Spring Boot logging configuration
- Standard Logback without JSON formatting
- Deferred structured logging to future tasks

Rationale:
- Minimal setup for scaffolding phase
- Ready for structured logging addition later
- No custom logging framework dependencies
- Follows minimal-dependencies principle

### 13. DTO Implementation Style

**Decision: Java Records for Immutable DTOs**

When appropriate, use Java `record` for request/response DTOs:

```java
public record LoginRequest(String email, String password) {}
public record LoginResponse(String accessToken, String tokenType, Long expiresIn) {}
```

Rationale:
- Concise immutable data structures
- Automatic equals/hashCode/toString
- Reduced boilerplate code
- Java 16+ language feature adoption

### 14. Shared Libraries

**Decision: No Shared Modules**

Each microservice remains completely autonomous:
- No shared parent modules
- No common libraries
- Minimal code duplication accepted for independence
- Test utilities duplicated per service as needed

Rationale:
- Preserves microservice independence
- Avoids implicit coupling
- Simplifies independent deployment
- Reduces shared testing infrastructure

Alternative considered: Create `services/shared` module. Rejected to maintain complete service autonomy.

### 15. Controller Implementations

**Decision: Scaffold Controller Classes Without Endpoints**

Each service includes:
- `@RestController` annotated controller classes
- No actual endpoints (`@GetMapping`, `@PostMapping`, etc.)
- No mock responses or demo endpoints
- Business logic deferred to implementation tasks

Rationale:
- Satisfies package organization requirements
- Prevents premature endpoint exposure
- Business implementation deferred explicitly
- Ready for incremental endpoint addition

## Traceability

| Reference | Document | Requirement |
|-----------|----------|-------------|
| HLD 06 | Architecture Overview | Microservices, Clean Architecture, Hexagonal Architecture |
| ADR-001 | Java 21 decision | Mandatory JDK version |
| ADR-005 | Kubernetes | Container-ready deployments |
| LLD Shared | Shared Architecture | Package organization, layers |
| TASK-002 | Project Scaffolding | Scaffold generation rules |

## Implementation Notes

This ADR applies to all four microservices:
1. Identity Service (`com.fiapx.identity`)
2. Video Service (`com.fiapx.video`)
3. Processing Worker (`com.fiapx.processing`)
4. Notification Service (`com.fiapx.notification`)

All future microservices must adopt these same conventions.

## Template Reference

The generated scaffold serves as the reference template for future microservices. Promotion to reusable templates under `.ai/templates/microservice/` requires explicit Software Architect approval.

## Constraints

No architectural decisions may introduce:
- Additional shared libraries without approval
- Alternative package naming schemes
- Different testing frameworks per service
- Non-standard configuration approaches
- Build tool alternatives without ADR amendment

All constraints are preserved from approved ADRs (001-010).

### 16. AWS SDK Versioning

**Decision: Use AWS SDK for Java v2 (BOM)**

All AWS integrations in the project SHALL use the AWS SDK for Java v2 BOM in Gradle builds. The BOM must be declared as:

```
implementation(platform("software.amazon.awssdk:bom:2.20.0"))
```

and service-specific modules included as needed, for example:

- `software.amazon.awssdk:s3`
- `software.amazon.awssdk:sns`
- `software.amazon.awssdk:sqs`

Rationale:
- v2 SDK provides non-blocking clients and improved configuration
- Single BOM ensures consistent versions across services

Traceability:
- Approved in Software Architect review during TASK-002.1

