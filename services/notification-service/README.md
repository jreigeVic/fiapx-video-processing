# Notification Service

Purpose
- Consume processing events and dispatch notifications (scaffold only).

Responsibilities (scaffold)
- Consume VideoProcessed and VideoFailed
- Register notification attempts

Architecture
- Java 21, Spring Boot 3.x, Gradle (Kotlin DSL), Flyway, PostgreSQL

Package structure
- com.fiapx.notification
  - infrastructure.adapter.in.messaging
  - application.usecase
  - domain.model

Build
- ./gradlew build

Run
- ./gradlew bootRun

Docker
- docker build -t notification-service:0.1.0 .

Tests
- ./gradlew test

Notes
- This project is a scaffold. No business logic or external integrations are implemented.

