# Processing Worker

Purpose
- Responsible for asynchronous processing of uploaded videos (scaffold only).

Responsibilities (scaffold)
- Consume VideoUploaded events
- Produce VideoProcessed / VideoFailed events

Architecture
- Java 21, Spring Boot 3.x, Gradle (Kotlin DSL), Flyway

Package structure
- com.fiapx.processing
  - application.usecase
  - infrastructure.adapter.in.messaging
  - infrastructure.adapter.out

Build
- ./gradlew build

Run
- ./gradlew bootRun

Docker
- docker build -t processing-worker:0.1.0 .

Tests
- ./gradlew test

Notes
- This project is a scaffold. No business logic or external integrations are implemented.

