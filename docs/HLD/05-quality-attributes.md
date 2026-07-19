# 05 - Quality Attributes

## Objetivo

Este documento apresenta os atributos de qualidade que orientam a arquitetura da plataforma **FIAP X Video Processing**.

Os atributos descritos representam características essenciais para garantir que a solução atenda aos objetivos de negócio definidos anteriormente e sustentam as decisões arquiteturais adotadas ao longo do projeto.

Cada atributo possui uma estratégia arquitetural correspondente, estabelecendo uma relação direta entre os requisitos não funcionais e a solução proposta.

---

# Visão Geral

A arquitetura foi projetada priorizando atributos de qualidade considerados essenciais para aplicações distribuídas executadas em ambientes Cloud Native.

Os principais atributos considerados durante o processo de arquitetura foram:

- Escalabilidade
- Disponibilidade
- Resiliência
- Segurança
- Observabilidade
- Baixo Acoplamento
- Manutenibilidade
- Performance
- Portabilidade
- Automação

---

# Atributos Arquiteturais

| Atributo | Objetivo | Estratégia Arquitetural |
|----------|----------|-------------------------|
| Escalabilidade | Suportar aumento de carga sem degradação significativa | Kubernetes com escalabilidade horizontal e processamento assíncrono |
| Disponibilidade | Garantir continuidade da operação mesmo diante de falhas | Serviços stateless, múltiplas réplicas e infraestrutura gerenciada |
| Resiliência | Evitar perda de processamento durante falhas | Arquitetura orientada a eventos, filas, DLQ, Retry e Idempotência |
| Segurança | Proteger usuários, dados e infraestrutura | Autenticação, autorização, gerenciamento seguro de segredos e comunicação segura |
| Observabilidade | Permitir acompanhamento completo da operação | Métricas, logs centralizados e rastreamento distribuído |
| Baixo Acoplamento | Reduzir dependências entre componentes | Microsserviços e comunicação baseada em eventos |
| Manutenibilidade | Facilitar evolução e manutenção do sistema | Clean Architecture, Hexagonal Architecture e separação de responsabilidades |
| Performance | Reduzir tempo de resposta percebido pelo usuário | Processamento assíncrono e desacoplamento entre requisição e processamento |
| Portabilidade | Facilitar execução em diferentes ambientes | Containers e Infraestrutura como Código |
| Automação | Reduzir atividades manuais durante entrega | Pipeline automatizada de integração e entrega contínua |

---

# Relação entre Requisitos e Arquitetura

Os atributos de qualidade apresentados neste documento influenciaram diretamente as principais decisões arquiteturais da solução.

Por exemplo:

| Requisito Não Funcional | Atributo de Qualidade Impactado |
|--------------------------|---------------------------------|
| Escalabilidade | Escalabilidade |
| Disponibilidade | Disponibilidade |
| Resiliência | Resiliência |
| Segurança | Segurança |
| Observabilidade | Observabilidade |
| Manutenibilidade | Manutenibilidade |
| Performance | Performance |
| Portabilidade | Portabilidade |
| Entregabilidade | Automação |

Essa rastreabilidade garante que cada decisão arquitetural possa ser justificada por um requisito de negócio ou operacional.

---

# Trade-offs Arquiteturais

A arquitetura proposta privilegia desacoplamento, escalabilidade e resiliência, aceitando um aumento controlado na complexidade operacional.

Os principais trade-offs assumidos foram:

- adoção de comunicação assíncrona em troca de maior desacoplamento entre serviços;
- maior quantidade de componentes distribuídos em troca de escalabilidade e evolução independente;
- aumento da complexidade operacional compensado por automação de infraestrutura e observabilidade.

Esses trade-offs são considerados adequados para uma plataforma Cloud Native baseada em microsserviços.

---

# Considerações

Os atributos de qualidade apresentados neste documento servem como referência para todas as decisões arquiteturais descritas nas próximas seções do High Level Design.

Toda decisão de arquitetura deverá preservar esses atributos ao longo da evolução da plataforma.