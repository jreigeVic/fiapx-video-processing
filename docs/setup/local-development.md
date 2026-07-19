# Local Development Infrastructure

This document describes how to start the local infrastructure required to develop the FIAP X Video Processing Platform.

It covers only local infrastructure. No CI/CD, Kubernetes, Terraform, AWS infrastructure provisioning or microservice implementation is described here.

---

## Scope

Docker Compose provisions the two infrastructure dependencies shared by all microservices:

- **PostgreSQL** — relational database (one logical database per service, per ADR-004 and ADR-007).
- **LocalStack** — local emulation of the AWS services used by the platform: S3, SNS and SQS (per ADR-002 and ADR-006).

Microservices themselves (`identity-service`, `video-service`, `processing-worker`, `notification-service`) are **not** started by this Docker Compose file. Run them individually (Gradle or your IDE) against the containers started here.

---

## Prerequisites

- Docker Desktop installed and running.

---

## Getting Started

1. Copy the environment template:

   ```bash
   cp .env.example .env
   ```

2. Start the infrastructure:

   ```bash
   docker compose up -d
   ```

3. Check that both containers report `healthy`:

   ```bash
   docker compose ps
   ```

4. Stop the infrastructure when no longer needed:

   ```bash
   docker compose down
   ```

   Add `-v` only if you intend to discard all persisted data (Postgres databases and LocalStack state):

   ```bash
   docker compose down -v
   ```

---

## Services

### PostgreSQL

- Image: `postgres:16`.
- Exposed on `localhost:${POSTGRES_PORT}` (default `5432`).
- Credentials and port are controlled by `POSTGRES_USER`, `POSTGRES_PASSWORD` and `POSTGRES_PORT` in `.env`.
- Data is persisted in the named volume `postgres_data`.
- Healthcheck: `pg_isready`.

#### Database initialization strategy

Each microservice owns its own logical database (Database per Service, per ADR-004). Rather than hardcoding database names in a SQL script, the Postgres container mounts `infrastructure/docker/postgres/init-databases.sh`, which reads the comma-separated `POSTGRES_MULTIPLE_DATABASES` variable and creates one database per entry, skipping any that already exist.

Default value:

```
POSTGRES_MULTIPLE_DATABASES=auth_db,video_db,processing_db,notification_db
```

This keeps the initialization script generic and reusable: adding a database for a future microservice only requires appending its name to `POSTGRES_MULTIPLE_DATABASES` in `.env` — the script itself never needs to change. This scales cleanly as the number of microservices grows, compared to maintaining a separate SQL file per service or hardcoding names in the script.

> Note: the databases above use the names already present in each service's `application.yml` (`auth_db`, `video_db`, `processing_db`, `notification_db`), matching ADR-012's naming examples.

### LocalStack

- Image: `localstack/localstack:3`.
- Exposed on `localhost:${LOCALSTACK_PORT}` (default `4566`).
- Emulated services are controlled by `LOCALSTACK_SERVICES` (default `s3,sns,sqs`).
- Data is persisted in the named volume `localstack_data` (`LOCALSTACK_PERSISTENCE=1`).
- Healthcheck: `GET /_localstack/health`.

No buckets, topics or queues are pre-created — resource provisioning is application/business concern and out of scope for this task.

---

## Network and Volumes

- Both services join the bridge network `fiapx-network`, allowing future service containers to reach them by hostname (`postgres`, `localstack`) if containerized later.
- Named volumes (`postgres_data`, `localstack_data`) persist data across `docker compose down`. They are Docker-managed (not bind mounts), so no data files are written into the repository.

---

## Environment Variables

All variables are documented with placeholders in [`.env.example`](../../.env.example). No real credentials are used — Postgres and LocalStack credentials are local-only development defaults, not secrets.

| Variable | Purpose | Default |
|---|---|---|
| `POSTGRES_USER` | Postgres superuser | `postgres` |
| `POSTGRES_PASSWORD` | Postgres superuser password | `postgres` |
| `POSTGRES_PORT` | Host port mapped to Postgres | `5432` |
| `POSTGRES_MULTIPLE_DATABASES` | Comma-separated list of logical databases to create | `auth_db,video_db,processing_db,notification_db` |
| `LOCALSTACK_PORT` | Host port mapped to LocalStack | `4566` |
| `LOCALSTACK_SERVICES` | AWS services emulated by LocalStack | `s3,sns,sqs` |
| `LOCALSTACK_DEBUG` | LocalStack debug logging | `0` |
| `LOCALSTACK_PERSISTENCE` | Persist LocalStack state to disk | `1` |

---

## Troubleshooting

- **Port already in use**: change `POSTGRES_PORT` or `LOCALSTACK_PORT` in `.env` if `5432`/`4566` are already bound on your machine.
- **Container unhealthy**: run `docker compose logs postgres` or `docker compose logs localstack` to inspect startup errors.
- **Stale data**: `docker compose down -v` removes the named volumes and starts from a clean state on the next `up`.
