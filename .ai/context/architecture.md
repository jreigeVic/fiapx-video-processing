# Architecture Overview

Estilo arquitetural

- Cloud Native
- Event Driven
- Clean Architecture
- Hexagonal Architecture
- DDD Lite

Microsserviços

- Identity
- Video
- Worker
- Notification

Banco

Database per Service

Comunicação

SNS

↓

SQS

↓

Consumidores

Storage

Amazon S3

Infraestrutura

Terraform

Kubernetes

Helm

Observabilidade

OpenTelemetry

↓

New Relic