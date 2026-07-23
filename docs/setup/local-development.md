# Infraestrutura de Desenvolvimento Local

Este documento descreve como iniciar a infraestrutura local necessária para desenvolver a FIAP X Video Processing Platform.

Ele cobre apenas a infraestrutura local. CI/CD, Kubernetes, Terraform, provisionamento de infraestrutura AWS ou implementação de microsserviços não são descritos aqui.

---

## Escopo

O Docker Compose provisiona as duas dependências de infraestrutura compartilhadas por todos os microsserviços:

- **PostgreSQL** — banco de dados relacional (um banco de dados lógico por serviço, conforme ADR-004 e ADR-007).
- **LocalStack** — emulação local dos serviços AWS utilizados pela plataforma: S3, SNS e SQS (conforme ADR-002 e ADR-006).

Os próprios microsserviços (`identity-service`, `video-service`, `processing-worker`, `notification-service`) **não** são iniciados por este arquivo Docker Compose. Execute-os individualmente (Gradle ou sua IDE) contra os containers iniciados aqui.

---

## Pré-requisitos

- Docker Desktop instalado e em execução.

---

## Como Começar

1. Copie o template de ambiente:

   ```bash
   cp .env.example .env
   ```

2. Inicie a infraestrutura:

   ```bash
   docker compose up -d
   ```

3. Verifique se ambos os containers reportam `healthy`:

   ```bash
   docker compose ps
   ```

4. Pare a infraestrutura quando não for mais necessária:

   ```bash
   docker compose down
   ```

   Adicione `-v` apenas se você pretende descartar todos os dados persistidos (bancos de dados do Postgres e estado do LocalStack):

   ```bash
   docker compose down -v
   ```

---

## Serviços

### PostgreSQL

- Imagem: `postgres:16`.
- Exposto em `localhost:${POSTGRES_PORT}` (padrão `5432`).
- Credenciais e porta são controladas por `POSTGRES_USER`, `POSTGRES_PASSWORD` e `POSTGRES_PORT` no `.env`.
- Os dados são persistidos no volume nomeado `postgres_data`.
- Healthcheck: `pg_isready`.

#### Estratégia de inicialização do banco de dados

Cada microsserviço possui seu próprio banco de dados lógico (Database per Service, conforme ADR-004). Em vez de fixar nomes de bancos de dados em um script SQL, o container do Postgres monta `infrastructure/docker/postgres/init-databases.sh`, que lê a variável `POSTGRES_MULTIPLE_DATABASES` (separada por vírgulas) e cria um banco de dados por entrada, ignorando os que já existem.

Valor padrão:

```
POSTGRES_MULTIPLE_DATABASES=auth_db,video_db,processing_db,notification_db
```

Isso mantém o script de inicialização genérico e reutilizável: adicionar um banco de dados para um futuro microsserviço requer apenas incluir seu nome em `POSTGRES_MULTIPLE_DATABASES` no `.env` — o script em si nunca precisa mudar. Isso escala de forma limpa conforme o número de microsserviços cresce, em comparação a manter um arquivo SQL separado por serviço ou fixar nomes diretamente no script.

> Nota: os bancos de dados acima usam os nomes já presentes no `application.yml` de cada serviço (`auth_db`, `video_db`, `processing_db`, `notification_db`), correspondendo aos exemplos de nomenclatura do ADR-012.

### LocalStack

- Imagem: `localstack/localstack:3`.
- Exposto em `localhost:${LOCALSTACK_PORT}` (padrão `4566`).
- Os serviços emulados são controlados por `LOCALSTACK_SERVICES` (padrão `s3,sns,sqs`).
- Os dados são persistidos no volume nomeado `localstack_data` (`LOCALSTACK_PERSISTENCE=1`).
- Healthcheck: `GET /_localstack/health`.

Nenhum bucket, tópico ou fila é pré-criado — o provisionamento de recursos é uma responsabilidade da aplicação/negócio e está fora do escopo desta tarefa.

---

## Rede e Volumes

- Ambos os serviços participam da rede bridge `fiapx-network`, permitindo que futuros containers de serviço os alcancem pelo hostname (`postgres`, `localstack`) caso sejam containerizados posteriormente.
- Os volumes nomeados (`postgres_data`, `localstack_data`) persistem os dados entre execuções de `docker compose down`. Eles são gerenciados pelo Docker (não são bind mounts), então nenhum arquivo de dados é gravado no repositório.

---

## Variáveis de Ambiente

Todas as variáveis estão documentadas com placeholders em [`.env.example`](../../.env.example). Nenhuma credencial real é utilizada — as credenciais do Postgres e do LocalStack são padrões de desenvolvimento apenas locais, não segredos.

| Variável | Finalidade | Padrão |
|---|---|---|
| `POSTGRES_USER` | Superusuário do Postgres | `postgres` |
| `POSTGRES_PASSWORD` | Senha do superusuário do Postgres | `postgres` |
| `POSTGRES_PORT` | Porta do host mapeada para o Postgres | `5432` |
| `POSTGRES_MULTIPLE_DATABASES` | Lista separada por vírgulas dos bancos de dados lógicos a serem criados | `auth_db,video_db,processing_db,notification_db` |
| `LOCALSTACK_PORT` | Porta do host mapeada para o LocalStack | `4566` |
| `LOCALSTACK_SERVICES` | Serviços AWS emulados pelo LocalStack | `s3,sns,sqs` |
| `LOCALSTACK_DEBUG` | Log de debug do LocalStack | `0` |
| `LOCALSTACK_PERSISTENCE` | Persistir o estado do LocalStack em disco | `1` |

---

## Solução de Problemas

- **Porta já em uso**: altere `POSTGRES_PORT` ou `LOCALSTACK_PORT` no `.env` caso `5432`/`4566` já estejam em uso na sua máquina.
- **Container unhealthy**: execute `docker compose logs postgres` ou `docker compose logs localstack` para inspecionar erros de inicialização.
- **Dados obsoletos (stale data)**: `docker compose down -v` remove os volumes nomeados e inicia a partir de um estado limpo no próximo `up`.
