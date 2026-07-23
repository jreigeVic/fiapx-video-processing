# ⚙️ Processing Worker

Faz polling dos vídeos enviados e extrai frames em um ZIP para download na Plataforma FIAP X de Processamento de Vídeo. Sem servidor HTTP por design ([`docs/LLD/processing-worker.md`](../../docs/LLD/processing-worker.md)) - é puramente um worker que faz polling no SQS.

## 📋 Responsabilidades

- Consome `VideoUploaded` (fila SQS `video-processing-queue`), baixa o original do S3, roda o ffmpeg para extrair frames, compacta o resultado e sobe de volta para o S3.
- Publica `VideoProcessed` ou `VideoFailed` (SNS) ao concluir/falhar.
- Idempotente via `processed_events` em `processing_db` (id/tipo do evento), para que mensagens SQS reentregues não reprocessem um vídeo duas vezes.
- A concorrência vem de múltiplas réplicas competindo na mesma fila (SQS competing consumers) mais o HPA, nunca de paralelismo interno no consumer (veja `infrastructure/helm/microservice/values-processing-worker.yaml`).

## 🏗️ Arquitetura

Java 21, Spring Boot 3.3, Gradle (Kotlin DSL), Flyway, PostgreSQL, AWS SDK v2 (S3, SNS, SQS), ffmpeg (instalado na imagem Docker). Clean/Hexagonal Architecture (`com.fiapx.processing.{application,domain,infrastructure,configuration}`), reforçada por ArchUnit. Veja [`docs/LLD/processing-worker.md`](../../docs/LLD/processing-worker.md).

## ⚙️ Configuração

Principais variáveis de ambiente (valores padrão em `application.yml`): `DB_USER`, `DB_PASSWORD`, `AWS_REGION`, `AWS_ENDPOINT_URL` (em branco na AWS), `VIDEO_S3_BUCKET`, `FFMPEG_BINARY`, `FFMPEG_FRAME_RATE`, `VIDEO_PROCESSING_QUEUE`, `VIDEO_PROCESSING_POLL_DELAY_MS`.

## ▶️ Build, execução e testes

```bash
./gradlew build
./gradlew bootRun
./gradlew test
docker build -t processing-worker:0.1.0 .
```
