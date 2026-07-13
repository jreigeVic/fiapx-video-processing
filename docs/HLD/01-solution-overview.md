# 01 - Solution Overview

## Objetivo

Construir uma plataforma Cloud Native para processamento assíncrono de vídeos utilizando uma arquitetura baseada em microsserviços, comunicação orientada a eventos e infraestrutura escalável na AWS.

O projeto foi desenvolvido como parte do Hackathon da Pós-Tech FIAP e possui como objetivo demonstrar a aplicação de práticas modernas de Arquitetura de Software, DevOps e Engenharia de Plataforma.

---

## Problema

O sistema legado realiza o processamento de vídeos de forma síncrona e monolítica.

Principais limitações:

- Processamento demorado
- Baixa escalabilidade
- Alto acoplamento
- Evolução difícil
- Baixa resiliência

---

## Solução

A solução utiliza:

- Microsserviços
- Event Driven Architecture
- Kubernetes
- Amazon S3
- Amazon SNS
- Amazon SQS
- PostgreSQL
- GitHub Actions

---

## Objetivos Arquiteturais

- Escalabilidade
- Alta disponibilidade
- Baixo acoplamento
- Resiliência
- Observabilidade
- Segurança
- Evolução independente
