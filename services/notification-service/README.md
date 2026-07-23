# 📧 Notification Service

Consome os resultados do processamento e envia e-mail para o dono do vídeo, atendendo o RF-07 da Plataforma FIAP X de Processamento de Vídeo. Sem servidor HTTP por design ([`docs/LLD/notification-service.md`](../../docs/LLD/notification-service.md)) - é puramente um worker que faz polling no SQS.

## 📋 Responsabilidades

- Consome `VideoProcessed`/`VideoFailed` (fila SQS `notification-queue`) e envia um e-mail via Amazon SES.
- Registra toda tentativa como uma `Notification` (`SENT` ou `FAILED`) em `notification_db`. Uma `MessageRejectedException` (ex.: identidade do SES sandbox não verificada - veja [ADR-016](../../docs/ADR/ADR-016-aws-academy-ses-verification-constraint.md)) é tratada como falha permanente de entrega: a notificação é marcada `FAILED` e o evento é confirmado (ack), em vez de ser retentado indefinidamente ou travar o poller.
- Idempotente via `processed_events` (id/tipo do evento), para que mensagens SQS reentregues não gerem envio em duplicidade.

## 🏗️ Arquitetura

Java 21, Spring Boot 3.3, Gradle (Kotlin DSL), Flyway, PostgreSQL, AWS SDK v2 (SES, SQS). Clean/Hexagonal Architecture (`com.fiapx.notification.{application,domain,infrastructure,configuration}`), reforçada por ArchUnit. Veja [`docs/LLD/notification-service.md`](../../docs/LLD/notification-service.md).

## ⚙️ Configuração

Principais variáveis de ambiente (valores padrão em `application.yml`): `DB_USER`, `DB_PASSWORD`, `AWS_REGION`, `AWS_ENDPOINT_URL` (em branco na AWS), `NOTIFICATION_SENDER_EMAIL`, `NOTIFICATION_QUEUE`, `NOTIFICATION_POLL_DELAY_MS`.

## ▶️ Build, execução e testes

```bash
./gradlew build
./gradlew bootRun
./gradlew test
docker build -t notification-service:0.1.0 .
```
