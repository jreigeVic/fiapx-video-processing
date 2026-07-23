# 🎬 FIAP X - Plataforma de Processamento de Vídeo Cloud Native

Plataforma cloud-native de processamento de vídeo desenvolvida para o Hackathon FIAP X: upload de vídeo, extração assíncrona de frames e download do resultado, construída como microsserviços orientados a eventos rodando em Kubernetes na AWS.

[![CI](https://github.com/jreigeVic/fiapx-video-processing/actions/workflows/ci.yml/badge.svg)](https://github.com/jreigeVic/fiapx-video-processing/actions/workflows/ci.yml)
[![CD](https://github.com/jreigeVic/fiapx-video-processing/actions/workflows/cd.yml/badge.svg)](https://github.com/jreigeVic/fiapx-video-processing/actions/workflows/cd.yml)

---

## 📚 Sumário

- [Visão geral](#-visão-geral)
- [Arquitetura](#-arquitetura)
- [Stack tecnológica](#-stack-tecnológica)
- [Como rodar localmente](#-como-rodar-localmente)
- [Documentação](#-documentação)
- [Estrutura do repositório](#-estrutura-do-repositório)

---

## 🧭 Visão geral

Um usuário se autentica, envia um vídeo, e a plataforma processa esse vídeo de forma assíncrona (extração de frames via `ffmpeg`), notificando o resultado por e-mail. Quatro microsserviços independentes cuidam de cada responsabilidade:

| Serviço | Responsabilidade | Docs |
|---|---|---|
| 🔐 [`identity-service`](services/identity-service) | Cadastro, login, JWT | [LLD](docs/LLD/identity-service.md) |
| 🎞️ [`video-service`](services/video-service) | Upload, status, download | [LLD](docs/LLD/video-service.md) |
| ⚙️ [`processing-worker`](services/processing-worker) | Extração de frames (ffmpeg) | [LLD](docs/LLD/processing-worker.md) |
| 📧 [`notification-service`](services/notification-service) | Notificação por e-mail (SES) | [LLD](docs/LLD/notification-service.md) |

## 🏗️ Arquitetura

- Clean Architecture + Hexagonal Architecture
- Domain-Driven Design (lite)
- Event-Driven Architecture (Amazon SNS + SQS, Competing Consumers)
- Database per Service
- Escalabilidade horizontal via HPA (Kubernetes)

> Este repositório segue um fluxo de engenharia AI-first: decisões arquiteturais são tomadas pelos engenheiros, e a implementação repetitiva é acelerada por IA. Veja [`docs/development/workflow.md`](docs/development/workflow.md).

## 🧰 Stack tecnológica

Java 21 · Spring Boot · PostgreSQL · Amazon S3/SNS/SQS · Kubernetes (EKS) · Terraform · GitHub Actions · OpenTelemetry · New Relic

## 🚀 Como rodar localmente

Pré-requisito: Docker Desktop instalado e em execução.

```bash
cp .env.example .env
docker compose up -d
```

Isso sobe **PostgreSQL** e **LocalStack** (S3/SNS/SQS emulados). Depois, rode cada microsserviço individualmente (Gradle ou sua IDE) contra esses containers. Guia completo em [`docs/setup/local-development.md`](docs/setup/local-development.md).

## 📖 Documentação

| O que você procura | Onde encontrar |
|---|---|
| 🗺️ Visão arquitetural completa (HLD) | [`docs/HLD/`](docs/HLD/README.md) |
| 🔍 Detalhes de implementação por serviço (LLD) | [`docs/LLD/`](docs/LLD/) |
| 📐 Decisões arquiteturais (ADRs) | [`docs/ADR/`](docs/ADR/README.md) |
| 🧩 Referência de API (endpoints, contratos) | [`docs/api/`](docs/api/README.md) |
| ⚙️ Setup de plataforma, CI/CD | [`docs/setup/`](docs/setup/) |
| 🧑‍💻 Fluxo de desenvolvimento, Definition of Done | [`docs/development/`](docs/development/workflow.md) |
| ✅ Rastreabilidade RF/RNF × evidências | [`docs/rf-rnf-traceability.md`](docs/rf-rnf-traceability.md) |
| 🕑 Histórico de decisões do projeto | [`docs/decision-log.md`](docs/decision-log.md) |
| 🛟 Problemas comuns e como resolver | [`docs/troubleshooting.md`](docs/troubleshooting.md) |
| 🧪 Testes de carga (k6) | [`tests/load/`](tests/load/README.md) |
| ☸️ Helm charts | [`infrastructure/helm/`](infrastructure/helm/README.md) |
| ☁️ Terraform (AWS) | [`infrastructure/`](infrastructure/README.md) |

## 📂 Estrutura do repositório

```
.
├── services/            # 4 microsservicos (Spring Boot / Java 21)
├── infrastructure/       # Terraform (AWS) + Helm (Kubernetes)
├── frontend/             # Demo estatico (login/upload/status/download)
├── tests/load/           # Cenarios de carga k6
├── docs/                 # HLD, LLD, ADRs, API, setup, desenvolvimento
└── .github/workflows/    # CI (ci.yml) e CD (cd.yml)
```
