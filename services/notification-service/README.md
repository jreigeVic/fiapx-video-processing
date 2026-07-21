# Notification Service

Consumes processing outcomes and emails the video's owner for the FIAP X Video Processing Platform (RF-07). No HTTP server by design (`docs/LLD/notification-service.md`) - purely an SQS-polling worker.

## Responsibilities

- Consumes `VideoProcessed`/`VideoFailed` (SQS `notification-queue`) and sends an email via Amazon SES.
- Records every attempt as a `Notification` (`SENT` or `FAILED`) in `notification_db`. A `MessageRejectedException` (e.g. unverified SES sandbox identity - see ADR-016) is treated as a permanent delivery failure: the notification is marked `FAILED` and the event is acknowledged, rather than retried indefinitely or crashing the poller.
- Idempotent via `processed_events` (event id/type), so redelivered SQS messages don't double-send.

## Architecture

Java 21, Spring Boot 3.3, Gradle (Kotlin DSL), Flyway, PostgreSQL, AWS SDK v2 (SES, SQS). Hexagonal/Clean Architecture (`com.fiapx.notification.{application,domain,infrastructure,configuration}`), enforced by ArchUnit. See `docs/LLD/notification-service.md`.

## Configuration

Key environment variables (defaults in `application.yml`): `DB_USER`, `DB_PASSWORD`, `AWS_REGION`, `AWS_ENDPOINT_URL` (blank in AWS), `NOTIFICATION_SENDER_EMAIL`, `NOTIFICATION_QUEUE`, `NOTIFICATION_POLL_DELAY_MS`.

## Build, run, test

```bash
./gradlew build
./gradlew bootRun
./gradlew test
docker build -t notification-service:0.1.0 .
```
