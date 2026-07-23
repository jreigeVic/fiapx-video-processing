# ADR-012 - Convencoes de Nomenclatura

Status: Accepted

Data: 2026-07-13

Responsavel: Software Architect

---

# Contexto

A medida que o projeto evolui com multiplos microsservicos, APIs, eventos, recursos de infraestrutura e documentacao, uma convencao de nomenclatura unica e necessaria para garantir consistencia, legibilidade e manutenibilidade.

Sem uma convencao comum, a documentacao e a implementacao podem divergir ao longo do tempo, aumentando o custo de manutencao e reduzindo a rastreabilidade.

---

# Decisao

O projeto adota uma unica estrategia de nomenclatura para todos os artefatos.

Este ADR define as convencoes oficiais de nomenclatura para toda a solucao.

---

# Convencao de Pacotes Java

Cada microsservico deve usar seu proprio pacote raiz.

Identity Service

com.fiapx.identity

Video Service

com.fiapx.video

Processing Worker

com.fiapx.processing

Notification Service

com.fiapx.notification

Nenhuma nomenclatura alternativa de pacote deve ser usada.

---

# Nomenclatura de Microsservicos

As pastas do repositorio devem usar kebab-case.

Exemplos

identity-service

video-service

processing-worker

notification-service

---

# Nomenclatura de Eventos

Os eventos de dominio devem usar PascalCase.

Exemplos

VideoUploaded

VideoProcessed

VideoFailed

Evitar:

video_uploaded

video-uploaded

video.uploaded.v1

Video_Uploaded

A menos que explicitamente aprovado pelo Software Architect.

---

# Nomenclatura de API

Os endpoints REST devem usar:

kebab-case

Exemplo

/api/v1/videos

/api/v1/auth

/api/v1/notifications

---

# Nomenclatura de Banco de Dados

Os bancos de dados logicos devem usar snake_case.

Exemplos

auth_db

video_db

notification_db

---

# Tabelas de Banco de Dados

As tabelas devem usar snake_case.

Exemplos

users

videos

video_processing

notifications

---

# Nomenclatura Flyway

Os arquivos de migracao devem seguir:

V<version>__<description>.sql

Exemplos

V001__create_users_table.sql

V002__create_video_table.sql

V003__add_processing_status.sql

---

# Imagens Docker

As imagens devem seguir:

fiapx/<service-name>

Exemplos

fiapx/identity-service

fiapx/video-service

fiapx/processing-worker

fiapx/notification-service

---

# Kubernetes

Deployments

identity-service

video-service

processing-worker

notification-service

Services

identity-service

video-service

processing-worker

notification-service

Namespaces

fiapx-dev

fiapx-hml

fiapx-prod

---

# Terraform

Os recursos devem seguir:

<provider>_<resource>_<service>

Exemplo

aws_s3_bucket_video

aws_sqs_processing

aws_sns_video

---

# Branches do Git

feature/<name>

bugfix/<name>

hotfix/<name>

release/<version>

---

# Tags do Git

v1.0.0

v1.1.0

v2.0.0

Semantic Versioning deve ser adotado.

---

# Nomenclatura de Documentacao

A documentacao deve usar kebab-case.

Exemplos

architecture-overview.md

video-service.md

processing-worker.md

event-catalog.md

---

# Mermaid

Os identificadores de diagrama devem usar PascalCase.

Os componentes devem usar os nomes oficiais dos servicos.

---

# Beneficios

Esta convencao proporciona:

- consistencia;
- rastreabilidade;
- legibilidade;
- onboarding mais facil;
- automacao mais simples;
- geracao assistida por IA mais facil;
- menor custo de manutencao.

---

# Consequencias

Toda documentacao e implementacao futura deve seguir este ADR.

Qualquer desvio requer aprovacao explicita do Software Architect.

---

# Referencias

High Level Design

Low Level Design

Architecture Rules

Documentation Rules