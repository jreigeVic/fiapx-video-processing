# AGENTS.md — AI Agent Developer Guide

This document guides AI agents in contributing to the FIAP X Video Processing Platform. Read this first before making any code changes.

## Architecture Overview

**FIAP X** is a cloud-native event-driven microservices platform for video processing. Key architectural principles:

- **Clean Architecture**: Controllers → Use Cases → Domain → Infrastructure  
- **Hexagonal Architecture**: Inbound adapters (HTTP, messaging) → Application → Outbound adapters (DB, S3, SNS/SQS)  
- **Database per Service**: Each microservice owns its PostgreSQL database (no cross-service DB access)  
- **Event-Driven Communication**: Async patterns via AWS SNS/SQS for inter-service communication  
- **Monorepo with Independent Services**: Four services in `services/` with independent Gradle builds

**The Four Services**:
- `identity-service` (com.fiapx.identity): JWT auth, user management  
- `video-service` (com.fiapx.video): Upload, metadata, S3 integration  
- `processing-worker` (com.fiapx.processing): Video encoding, event consumption  
- `notification-service` (com.fiapx.notification): Alerts on processing completion  

## Build & Development Workflows

**Each service is independent**. Build commands must run from the service directory:

```bash
cd services/identity-service
./gradlew.bat build          # Windows  
./gradlew build              # Linux/Mac  
./gradlew build -x test      # Skip tests  
./gradlew test               # Unit tests only  
./gradlew test --tests "*IntegrationTest"  # Integration tests  
```

**Key Gradle facts**:
- Each service has independent `build.gradle.kts` (NOT a multi-project build)  
- Java 21 with explicit Gradle toolchain configuration  
- Spring Boot 3.2.0, Gradle Kotlin DSL (.kts syntax)  
- All AWS SDK clients use the unified BOM: `software.amazon.awssdk:bom:2.20.0`  

**Docker builds** (creates Alpine image with fat JAR):
```bash
cd services/<service-name>
docker build -t fiapx/<service-name> .
```

## Naming Conventions (STRICT)

**Never deviate without architect approval.**

| Artifact | Convention | Example |
|----------|-----------|---------|
| **Java packages** | `com.fiapx.<service>` | `com.fiapx.video` (not `com.fiapx.videoservice`) |
| **Service folders** | kebab-case | `video-service` (not `videoService`) |
| **Docker images** | `fiapx/<service-name>` | `fiapx/video-service` |
| **Events** | PascalCase (no versions!) | `VideoUploaded` (NOT `video_uploaded`, `VideoUploadedV1`) |
| **REST endpoints** | kebab-case | `/api/v1/videos` (NOT `/api/v1/Videos`) |
| **Databases** | snake_case | `video_db`, `identity_db` |
| **DB tables** | snake_case | `videos`, `users` |
| **DB migrations** | Flyway pattern | `V001__create_users_table.sql` |

## Package Organization (Mandatory Layering)

**All services follow this structure** (omit unused layers):

```
com.fiapx.<service>
├── api/                     # REST controllers, request/response classes
│   ├── controller/
│   ├── request/             # DTOs as Java records
│   ├── response/
│   └── mapper/              # DTO ↔ Domain mappers
├── application/             # Business logic (use cases)
│   ├── usecase/             # Service actions
│   ├── port/                # Input/Output interfaces (Hexagonal)
│   │   ├── in/
│   │   └── out/
│   └── dto/                 # Application DTOs
├── domain/                  # Core business rules
│   ├── model/               # Entities
│   ├── valueobject/         # Immutable value types
│   ├── service/             # Domain services (if needed)
│   └── exception/           # Domain-specific exceptions
├── infrastructure/          # External integrations (adapters)
│   ├── adapter/
│   │   ├── in/web/          # HTTP adapters (controllers)
│   │   ├── in/messaging/    # Event consumers (SQS)
│   │   ├── out/persistence/ # JPA repositories
│   │   ├── out/storage/     # S3 client adapters
│   │   └── out/messaging/   # SNS/SQS publishers
│   └── config/              # Spring configuration
├── configuration/           # Application-level Spring config
└── shared/                  # Logging, error handling
    ├── error/
    └── observability/
```

**Critical rule**: Controllers have NO business logic. Use cases live in `application/`. Domain never imports Spring or AWS SDK.

## Event Patterns & Integration

**All events use this envelope** (mandatory):

```json
{
  "eventId": "<uuid>",
  "eventType": "VideoUploaded",
  "occurredAt": "2026-01-01T00:00:00Z",
  "correlationId": "<uuid>",
  "producer": "Video Service",
  "payload": { /* domain-specific data */ }
}
```

**Idempotency requirement**: Consumers must track processed `eventId` to avoid duplicates.  
**Correlation**: Propagate `correlationId` across HTTP, events, and logs for tracing.

**Current events** in use:
- `VideoUploaded` (Video Service → SQS → Processing Worker)  
- `VideoProcessed` (Processing Worker → SNS → Notification Service)  
- `VideoFailed` (Processing Worker → SNS → Notification Service)

## Error Model (Consistent Across All Services)

All HTTP errors return this JSON payload (NEVER expose secrets/tokens):

```json
{
  "timestamp": "2026-01-01T00:00:00Z",
  "status": 400,
  "error": "VALIDATION_ERROR",
  "message": "Invalid request parameters",
  "path": "/api/videos"
}
```

Use Spring exception handlers (`@ControllerAdvice`) to map domain exceptions to HTTP responses.

## Security & Configuration

- **JWT**: Identity Service issues tokens; Video Service validates them for protected endpoints  
- **Secrets**: Store credentials in Kubernetes Secrets, NOT in code  
- **Spring profiles**: `local`, `dev`, `test`, `prod` (via `application-{profile}.yml`)  
- **Database per service**: No service reads another service's database  

## Testing Strategy

- **Unit tests**: Domain, Application (use mocks) — run by default  
- **Integration tests**: Repositories, consumers, adapters using Testcontainers & LocalStack  
- **Test naming**: `*Test` (unit), `*IntegrationTest` (integration)  
- **Test location**: `src/test/java/com/fiapx/<service>/unit/` and `.../integration/`  
- **Avoid**: Testing getters/setters; focus on behavior and integration contracts  

## Key Documentation References

- **HLD** (`docs/HLD/`): Architecture, requirements, decisions  
- **LLD** (`docs/LLD/shared-architecture.md`): Shared patterns for all services  
- **ADRs** (`docs/ADR/ADR-011-*.md`): Binding architectural decisions  
- **API** (`docs/api/*.md`): Service-specific API contracts  
- **Conventions** (`docs/shared/`): Event, error, and security patterns  

## Before Writing Code

1. ✅ Check ADR-011 (microservice scaffolding rules)  
2. ✅ Confirm package structure matches above layers  
3. ✅ Verify naming follows conventions (especially events & endpoints)  
4. ✅ If consuming events, implement idempotency check  
5. ✅ Never make domain depend on Spring or AWS SDK  
6. ✅ Always use records for immutable DTOs  
7. ✅ Propagate `correlationId` in logs and downstream calls  

## Helpful Commands (Run from Service Directory)

```bash
# Compile without running tests
./gradlew compileJava

# View dependency tree (helps with version conflicts)
./gradlew dependencies

# Format code (if Spotless configured)
./gradlew spotlessApply

# Run specific test class
./gradlew test --tests "com.fiapx.video.domain.VideoTest"

# Check for vulnerabilities
./gradlew dependencyCheck
```

## Common Pitfalls to Avoid

1. ❌ **Hardcoded credentials** — Use environment variables or Kubernetes Secrets  
2. ❌ **Cross-service DB access** — Each service owns only its database  
3. ❌ **Sync when async expected** — Use SNS/SQS for event-driven communication  
4. ❌ **Version suffixes in events** — `VideoUploaded` not `VideoUploadedV1`  
5. ❌ **Business logic in controllers** — Move to use cases in `application/`  
6. ❌ **Shared libraries** — Duplicate code across services to maintain independence  
7. ❌ **Mixing naming conventions** — Stick to prescribed patterns per artifact type  

## Emergency Contact

If architecture seems unclear or a pattern isn't documented, check:
1. The LLD shared architecture (`docs/LLD/shared-architecture.md`)  
2. The relevant service's LLD (`docs/LLD/<service>-service.md`)  
3. ADR-011 for binding decisions  
4. Ask via comments referencing the ADR or HLD section

---

**Last Updated**: 2026-07-13  
**Project**: FIAP X Video Processing Platform  
**AI-Ready**: Yes — Follow this guide for immediate productivity.

