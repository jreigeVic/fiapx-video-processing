# 🎞️ Video Service

Cuida do upload, listagem, consulta de status e download de vídeos na Plataforma FIAP X de Processamento de Vídeo.

## 📋 Responsabilidades

- `POST /api/videos` (upload, validado por allowlist de content-type e tamanho máximo), `GET /api/videos`, `GET /api/videos/{id}`, `GET /api/videos/{id}/download` (URL pré-assinada do S3) - veja [`docs/api/video.md`](../../docs/api/video.md) e [`docs/api/openapi.yaml`](../../docs/api/openapi.yaml).
- Publica `VideoUploaded` (SNS) após armazenar o arquivo original no S3; consome `VideoProcessed`/`VideoFailed` (SQS) para atualizar o status.
- É dono exclusivo de `video_db`.

## 🏗️ Arquitetura

Java 21, Spring Boot 3.3, Gradle (Kotlin DSL), Flyway, PostgreSQL, AWS SDK v2 (S3, SNS, SQS). Clean/Hexagonal Architecture (`com.fiapx.video.{api,application,domain,infrastructure,configuration}`), reforçada por ArchUnit. Veja [`docs/LLD/video-service.md`](../../docs/LLD/video-service.md) para o design completo.

## ⚙️ Configuração

Principais variáveis de ambiente (valores padrão em `application.yml`): `DB_USER`, `DB_PASSWORD`, `JWT_SECRET` (precisa ser igual ao do identity-service), `AWS_REGION`, `AWS_ENDPOINT_URL` (em branco na AWS - só usado para LocalStack), `VIDEO_S3_BUCKET`, `VIDEO_UPLOAD_MAX_FILE_SIZE_BYTES`, `VIDEO_UPLOAD_ALLOWED_CONTENT_TYPES`, `VIDEO_CORS_ALLOWED_ORIGINS`, `PORT` (padrão `8082`).

## ▶️ Build, execução e testes

```bash
./gradlew build
./gradlew bootRun
./gradlew test
docker build -t video-service:0.1.0 .
```

Testes de integração que precisam de Postgres usam Testcontainers (exige um daemon Docker local). Swagger UI ao vivo: `/swagger-ui/index.html`.
