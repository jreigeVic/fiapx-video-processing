# Video Service

Handles video upload, listing, status lookup and download for the FIAP X Video Processing Platform.

## Responsibilities

- `POST /api/videos` (upload, validated by content-type allowlist and max size), `GET /api/videos`, `GET /api/videos/{id}`, `GET /api/videos/{id}/download` (presigned S3 URL) - see `docs/api/video.md` and `docs/api/openapi.yaml`.
- Publishes `VideoUploaded` (SNS) after storing the original file in S3; consumes `VideoProcessed`/`VideoFailed` (SQS) to update status.
- Owns `video_db` exclusively.

## Architecture

Java 21, Spring Boot 3.3, Gradle (Kotlin DSL), Flyway, PostgreSQL, AWS SDK v2 (S3, SNS, SQS). Hexagonal/Clean Architecture (`com.fiapx.video.{api,application,domain,infrastructure,configuration}`), enforced by ArchUnit. See `docs/LLD/video-service.md` for the full design.

## Configuration

Key environment variables (defaults in `application.yml`): `DB_USER`, `DB_PASSWORD`, `JWT_SECRET` (must match identity-service's), `AWS_REGION`, `AWS_ENDPOINT_URL` (blank in AWS - only set for LocalStack), `VIDEO_S3_BUCKET`, `VIDEO_UPLOAD_MAX_FILE_SIZE_BYTES`, `VIDEO_UPLOAD_ALLOWED_CONTENT_TYPES`, `VIDEO_CORS_ALLOWED_ORIGINS`, `PORT` (default `8082`).

## Build, run, test

```bash
./gradlew build
./gradlew bootRun
./gradlew test
docker build -t video-service:0.1.0 .
```

Integration tests requiring Postgres use Testcontainers (needs a local Docker daemon). Live Swagger UI: `/swagger-ui/index.html`.
