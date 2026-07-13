# Video API

## Objetivo

Documentar os endpoints HTTP do Video Service para upload, consulta, historico e download de resultados.

## Base Path

`/api/videos`

## Autenticacao

Todos os endpoints exigem JWT valido.

## Endpoints

### POST /api/videos

Envia um video para processamento assincrono.

#### Request

`multipart/form-data`

| Campo | Tipo | Obrigatorio |
|-------|------|-------------|
| file | binary | Sim |

#### Response 202

```json
{
  "videoId": "uuid",
  "status": "PROCESSING"
}
```

#### Status Codes

| Status | Motivo |
|--------|--------|
| 202 | Video recebido para processamento. |
| 400 | Arquivo invalido. |
| 401 | Token ausente ou invalido. |
| 500 | Falha nao esperada. |

### GET /api/videos

Lista o historico de videos do usuario autenticado.

#### Query Parameters

| Nome | Tipo | Obrigatorio | Descricao |
|------|------|-------------|-----------|
| status | string | Nao | Filtra por RECEIVED, PROCESSING, PROCESSED ou FAILED. |

#### Response 200

```json
[
  {
    "id": "uuid",
    "originalFileName": "video.mp4",
    "status": "PROCESSED",
    "createdAt": "2026-01-01T00:00:00Z",
    "updatedAt": "2026-01-01T00:10:00Z",
    "downloadAvailable": true
  }
]
```

### GET /api/videos/{videoId}

Consulta detalhe e status de um video do usuario autenticado.

#### Path Parameters

| Nome | Tipo | Obrigatorio |
|------|------|-------------|
| videoId | uuid | Sim |

#### Response 200

```json
{
  "id": "uuid",
  "originalFileName": "video.mp4",
  "status": "PROCESSING",
  "createdAt": "2026-01-01T00:00:00Z",
  "updatedAt": "2026-01-01T00:01:00Z",
  "downloadAvailable": false
}
```

#### Status Codes

| Status | Motivo |
|--------|--------|
| 200 | Video encontrado. |
| 401 | Token ausente ou invalido. |
| 404 | Video inexistente ou nao pertence ao usuario. |

### GET /api/videos/{videoId}/download

Gera URL temporaria para download do ZIP processado.

#### Response 200

```json
{
  "videoId": "uuid",
  "url": "https://s3-presigned-url",
  "expiresAt": "2026-01-01T01:00:00Z"
}
```

#### Status Codes

| Status | Motivo |
|--------|--------|
| 200 | URL gerada. |
| 401 | Token ausente ou invalido. |
| 404 | Video inexistente ou nao pertence ao usuario. |
| 409 | Resultado ainda nao esta disponivel. |

## Fluxo

```mermaid
sequenceDiagram
  participant User
  participant VideoService
  participant S3
  participant SNS
  participant VideoDB

  User->>VideoService: POST /api/videos
  VideoService->>VideoDB: Cria registro
  VideoService->>S3: Upload arquivo
  VideoService->>SNS: Publica VideoUploaded
  VideoService-->>User: 202 videoId
```

## Erros Possiveis

Erros seguem o contrato compartilhado. O Video Service nao deve expor chaves internas do S3, stack traces ou detalhes de infraestrutura.
