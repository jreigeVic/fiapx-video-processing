# Processing Worker

Polls uploaded videos and extracts frames into a downloadable ZIP for the FIAP X Video Processing Platform. No HTTP server by design (`docs/LLD/processing-worker.md`) - purely an SQS-polling worker.

## Responsibilities

- Consumes `VideoUploaded` (SQS `video-processing-queue`), downloads the original from S3, runs ffmpeg to extract frames, zips the result and uploads it back to S3.
- Publishes `VideoProcessed` or `VideoFailed` (SNS) on completion/failure.
- Idempotent via `processed_events` in `processing_db` (event id/type), so redelivered SQS messages don't reprocess a video twice.
- Concurrency comes from multiple replicas competing on the same queue (SQS competing consumers) plus HPA, never from internal parallelism in the consumer (see `infrastructure/helm/microservice/values-processing-worker.yaml`).

## Architecture

Java 21, Spring Boot 3.3, Gradle (Kotlin DSL), Flyway, PostgreSQL, AWS SDK v2 (S3, SNS, SQS), ffmpeg (installed in the Docker image). Hexagonal/Clean Architecture (`com.fiapx.processing.{application,domain,infrastructure,configuration}`), enforced by ArchUnit. See `docs/LLD/processing-worker.md`.

## Configuration

Key environment variables (defaults in `application.yml`): `DB_USER`, `DB_PASSWORD`, `AWS_REGION`, `AWS_ENDPOINT_URL` (blank in AWS), `VIDEO_S3_BUCKET`, `FFMPEG_BINARY`, `FFMPEG_FRAME_RATE`, `VIDEO_PROCESSING_QUEUE`, `VIDEO_PROCESSING_POLL_DELAY_MS`.

## Build, run, test

```bash
./gradlew build
./gradlew bootRun
./gradlew test
docker build -t processing-worker:0.1.0 .
```
