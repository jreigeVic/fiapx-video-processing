# Identity Service

Purpose
- Responsible for user authentication and authorization (scaffold only).

Responsibilities (scaffold)
- Authentication
- Authorization
- Users

Architecture
- Java 21, Spring Boot 3.x, Gradle (Kotlin DSL), Flyway, PostgreSQL

Package structure
- com.fiapx.identity
  - api.controller
  - application.usecase
  - domain.model
  - infrastructure

Build
- ./gradlew build

Run
- ./gradlew bootRun

Docker
- docker build -t identity-service:0.1.0 .

Tests
- ./gradlew test

Notes
- This project is a scaffold. No business logic or external integrations are implemented.

