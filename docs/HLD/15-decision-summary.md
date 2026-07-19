# 15 - Architecture Decision Summary

## Objetivo

Este documento consolida as principais decisões arquiteturais adotadas durante o desenvolvimento da plataforma **FIAP X Video Processing**.

Seu objetivo é fornecer uma visão executiva das decisões mais relevantes que orientaram a arquitetura da solução, servindo como referência para os documentos de Architecture Decision Records (ADRs) e para o Low Level Design (LLD).

---

# Visão Geral

Durante a definição da arquitetura foram avaliadas diferentes alternativas tecnológicas e arquiteturais.

As decisões apresentadas neste documento refletem as escolhas consideradas mais adequadas para atender aos requisitos funcionais, não funcionais e aos atributos de qualidade definidos anteriormente.

---

# Decisões Arquiteturais

## Arquitetura baseada em Microsserviços

### Decisão

A solução foi organizada em microsserviços independentes.

### Motivação

Permitir evolução, implantação e escalabilidade independentes para cada domínio de negócio.

---

## Arquitetura Orientada a Eventos

### Decisão

A comunicação entre os serviços ocorre prioritariamente por meio de eventos assíncronos.

### Motivação

Reduzir o acoplamento entre componentes e permitir processamento desacoplado.

---

## Database per Service

### Decisão

Cada microsserviço possui seu próprio banco de dados.

### Motivação

Garantir isolamento entre domínios e autonomia de evolução.

---

## Cloud Native

### Decisão

A solução foi projetada para execução em ambiente Kubernetes utilizando serviços gerenciados da AWS.

### Motivação

Aumentar disponibilidade, escalabilidade e automação operacional.

---

## Processamento Assíncrono

### Decisão

Operações intensivas são executadas em segundo plano.

### Motivação

Melhorar a experiência do usuário e aumentar a capacidade de processamento da plataforma.

---

## Armazenamento de Arquivos

### Decisão

Os vídeos enviados e os resultados do processamento permanecem armazenados em serviço de armazenamento de objetos.

### Motivação

Separar armazenamento persistente do ciclo de vida dos containers e facilitar escalabilidade.

---

## Observabilidade

### Decisão

A solução foi projetada para disponibilizar métricas, logs e rastreamento distribuído desde sua concepção.

### Motivação

Facilitar monitoramento operacional e investigação de incidentes.

---

## Infraestrutura como Código

### Decisão

Toda a infraestrutura será provisionada utilizando Infraestrutura como Código.

### Motivação

Garantir reprodutibilidade, padronização e versionamento da infraestrutura.

---

## Pipeline Automatizada

### Decisão

Todo o processo de integração e entrega será automatizado.

### Motivação

Reduzir atividades manuais, aumentar confiabilidade e garantir qualidade das entregas.

---

# Trade-offs

Durante a definição da arquitetura foram assumidos alguns trade-offs.

| Decisão | Benefício | Trade-off |
|----------|-----------|-----------|
| Microsserviços | Evolução independente | Maior complexidade operacional |
| Comunicação por Eventos | Baixo acoplamento | Consistência eventual |
| Kubernetes | Escalabilidade | Curva de aprendizado maior |
| Database per Service | Isolamento entre domínios | Maior número de bancos lógicos |
| Processamento Assíncrono | Melhor experiência do usuário | Maior complexidade de monitoramento |

---

# Evolução Arquitetural

A arquitetura foi concebida para permitir evolução incremental da plataforma.

Entre as evoluções previstas destacam-se:

- API Gateway dedicado;
- Analytics Service;
- Audit Service;
- Notification Channels adicionais;
- KEDA para escalabilidade baseada em eventos;
- AsyncAPI;
- OpenAPI First;
- Spring Authorization Server;
- Event Versioning;
- Prometheus e Grafana como alternativa de observabilidade.

Essas evoluções podem ser incorporadas sem alterações significativas na estrutura atual da solução.

---

# Conclusão

As decisões arquiteturais apresentadas neste documento estabelecem uma base sólida para o desenvolvimento da plataforma, priorizando escalabilidade, resiliência, observabilidade, segurança e evolução contínua.

A arquitetura proposta atende aos objetivos definidos para o MVP e fornece flexibilidade suficiente para suportar futuras expansões da solução, preservando baixo acoplamento entre os componentes e alinhamento com princípios modernos de arquitetura Cloud Native.