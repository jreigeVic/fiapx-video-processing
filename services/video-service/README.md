# Video Service

Purpose
- Responsible for video uploads, metadata and processing status (scaffold only).

Responsibilities (scaffold)
- Uploads
- Metadata
- Processing status

Architecture
- Java 21, Spring Boot 3.x, Gradle (Kotlin DSL), Flyway, PostgreSQL

Package structure
- com.fiapx.video
  - api.controller
  - application.usecase
  - domain.model
  - infrastructure

Build
- ./gradlew build

Run
- ./gradlew bootRun

Docker
- docker build -t video-service:0.1.0 .

Tests
- ./gradlew test

Notes
- This project is a scaffold. No business logic or external integrations are implemented.

