# Database Model

## Objetivo

Representar os modelos de dados por servico, preservando Database per Service.

## Visao Geral

```mermaid
flowchart LR
  Auth[(auth_db)]
  Video[(video_db)]
  Processing[(processing_db)]
  Notification[(notification_db)]

  Identity["Identity Service"] --> Auth
  VideoService["Video Service"] --> Video
  ProcessingWorker["Processing Worker"] --> Processing
  NotificationService["Notification Service"] --> Notification
```

## auth_db

```mermaid
erDiagram
  users {
    uuid id PK
    string name
    string email UK
    string password_hash
    timestamp created_at
  }
```

## video_db

```mermaid
erDiagram
  videos {
    uuid id PK
    uuid owner_user_id
    string original_file_name
    string source_object_key
    string result_object_key
    string status
    string failure_reason
    timestamp created_at
    timestamp updated_at
  }

  processed_events {
    uuid event_id PK
    string event_type
    timestamp processed_at
  }
```

## processing_db

```mermaid
erDiagram
  processed_events {
    uuid event_id PK
    string event_type
    timestamp processed_at
  }
```

## notification_db

```mermaid
erDiagram
  notifications {
    uuid id PK
    uuid video_id
    uuid owner_user_id
    string type
    string status
    timestamp created_at
    timestamp sent_at
  }

  processed_events {
    uuid event_id PK
    string event_type
    timestamp processed_at
  }
```

## Regras

- Nenhum servico acessa diretamente banco de outro servico.
- O Processing Worker nao atualiza o video_db.
- Flyway deve versionar migracoes por servico.
