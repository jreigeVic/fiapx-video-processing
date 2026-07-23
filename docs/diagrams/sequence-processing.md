# Diagrama de Sequencia - Processamento de Video

## Objetivo

Representar o processamento assincrono iniciado pelo evento `VideoUploaded`.

```mermaid
sequenceDiagram
  autonumber
  participant SNS as Amazon SNS
  participant SQS as SQS Processing
  participant Worker as Processing Worker
  participant S3 as Amazon S3
  participant FFmpeg as FFmpeg
  participant ResultSNS as Amazon SNS

  SNS->>SQS: Entrega VideoUploaded
  SQS->>Worker: Consome VideoUploaded
  Worker->>Worker: Verifica idempotencia
  Worker->>S3: Baixa video original
  Worker->>FFmpeg: Extrai frames
  Worker->>Worker: Gera ZIP
  Worker->>S3: Envia ZIP
  Worker->>ResultSNS: Publica VideoProcessed
```

## Fluxo de Falha

```mermaid
sequenceDiagram
  participant SQS as SQS Processing
  participant Worker as Processing Worker
  participant SNS as Amazon SNS

  SQS->>Worker: VideoUploaded
  Worker->>Worker: Falha conhecida no processamento
  Worker->>SNS: Publica VideoFailed
```

## Regras

- O Worker nunca atualiza o video_db.
- Retry e DLQ pertencem a fila SQS.
- O resultado retorna ao Video Service por evento.
