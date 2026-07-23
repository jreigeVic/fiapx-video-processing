# Diagrama de Sequencia - Notificacao de Processamento

## Objetivo

Representar o fluxo de notificacao baseado em `VideoProcessed` e `VideoFailed`.

```mermaid
sequenceDiagram
  autonumber
  participant SNS as Amazon SNS
  participant SQS as SQS Notification
  participant Notification as Notification Service
  participant DB as notification_db
  participant Email as Servico de Email

  SNS->>SQS: Entrega VideoProcessed ou VideoFailed
  SQS->>Notification: Consome evento
  Notification->>Notification: Verifica idempotencia
  Notification->>DB: Registra tentativa
  Notification->>Email: Envia notificacao
  Notification->>DB: Atualiza status SENT
```

## Regras

- Notification Service nao altera video_db.
- Notificacoes duplicadas devem ser evitadas por idempotencia.
- Falhas temporarias devem usar retry da fila.
