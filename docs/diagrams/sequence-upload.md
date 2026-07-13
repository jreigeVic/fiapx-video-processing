# Sequence - Upload de Video

## Objetivo

Representar o fluxo de upload, armazenamento e publicacao do evento `VideoUploaded`.

```mermaid
sequenceDiagram
  autonumber
  participant User as Usuario
  participant Video as Video Service
  participant DB as video_db
  participant S3 as Amazon S3
  participant SNS as Amazon SNS

  User->>Video: POST /api/videos
  Video->>Video: Valida JWT e arquivo
  Video->>DB: Cria Video RECEIVED
  Video->>S3: Armazena video original
  Video->>SNS: Publica VideoUploaded
  Video->>DB: Atualiza status PROCESSING
  Video-->>User: 202 Accepted com videoId
```

## Regras

- O upload nao executa processamento pesado.
- O arquivo original fica no S3.
- O processamento inicia somente por evento.
