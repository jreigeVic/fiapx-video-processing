# 13 - Scalability

## Objetivo

Este documento apresenta a estratégia de escalabilidade da plataforma **FIAP X Video Processing**, descrevendo como a arquitetura foi projetada para suportar crescimento de demanda de forma eficiente, resiliente e sustentável.

A estratégia adotada busca permitir expansão da capacidade computacional sem necessidade de alterações na lógica da aplicação.

---

# Visão Geral

A plataforma foi concebida seguindo princípios de Cloud Native, permitindo que seus componentes sejam escalados de forma independente conforme a demanda.

A separação entre microsserviços e a utilização de processamento assíncrono reduzem o acoplamento entre os domínios e possibilitam o crescimento horizontal dos componentes mais demandados.

Essa abordagem permite utilizar recursos computacionais de maneira mais eficiente, evitando o escalonamento desnecessário de toda a aplicação.

---

# Estratégia de Escalabilidade

A solução adota escalabilidade horizontal como principal estratégia de crescimento.

Cada microsserviço pode aumentar ou reduzir sua capacidade de processamento independentemente dos demais, permitindo que a infraestrutura acompanhe o comportamento da carga de trabalho.

Essa estratégia reduz custos operacionais e melhora a utilização dos recursos da plataforma.

---

# Escalabilidade dos Microsserviços

Cada microsserviço possui características próprias de consumo e pode ser escalado individualmente.

| Serviço | Estratégia |
|----------|------------|
| Identity Service | Escalonamento conforme volume de autenticações |
| Video Service | Escalonamento conforme volume de uploads e consultas |
| Processing Worker | Escalonamento conforme quantidade de mensagens na fila |
| Notification Service | Escalonamento conforme volume de notificações |

---

# Processamento Assíncrono

O processamento de vídeos ocorre de forma desacoplada da interação do usuário.

Essa decisão arquitetural permite que os Workers sejam escalados conforme o volume de mensagens pendentes, sem impactar os demais serviços da plataforma.

Essa abordagem também evita que operações demoradas comprometam o tempo de resposta das APIs.

---

# Kubernetes

A plataforma utiliza Kubernetes como plataforma de orquestração dos containers.

Entre os principais recursos utilizados destacam-se:

- Deployments;
- Services;
- Horizontal Pod Autoscaler (HPA);
- Health Checks;
- Rolling Updates;
- Auto Recovery.

Esses recursos permitem distribuir a carga entre múltiplas instâncias e aumentar a disponibilidade da aplicação.

---

# Escalabilidade Baseada em Eventos

A utilização de mensageria desacopla a produção e o consumo dos eventos da plataforma.

O aumento da demanda gera crescimento na fila de processamento, permitindo que novos Workers sejam adicionados sem necessidade de alterações no restante da solução.

Essa abordagem reduz gargalos e melhora a utilização dos recursos computacionais.

---

# Evolução da Estratégia

A arquitetura foi projetada para permitir evolução gradual da estratégia de escalabilidade.

Entre as evoluções previstas destacam-se:

- escalabilidade baseada em métricas de filas;
- escalabilidade baseada em eventos;
- otimização automática de recursos;
- inclusão de novos consumidores especializados.

Essas evoluções poderão ser implementadas sem alterações significativas na arquitetura da plataforma.

---

# Benefícios

A estratégia adotada proporciona diversos benefícios operacionais.

Entre eles destacam-se:

- crescimento horizontal dos serviços;
- melhor utilização da infraestrutura;
- redução de custos computacionais;
- maior disponibilidade;
- maior capacidade de processamento;
- evolução independente dos componentes.

---

# Considerações

A estratégia de escalabilidade apresentada neste documento está diretamente relacionada aos atributos de qualidade definidos anteriormente e representa um dos principais pilares da arquitetura da plataforma.

Os detalhes de configuração do Kubernetes, políticas de autoscaling e infraestrutura serão apresentados posteriormente no Low Level Design e nos artefatos de infraestrutura como código.

---

## Evolução Arquitetural

A versão inicial da plataforma utiliza o Horizontal Pod Autoscaler (HPA) para escalabilidade automática dos microsserviços.

Como evolução arquitetural, está prevista a adoção do Kubernetes Event-Driven Autoscaler (KEDA), permitindo que componentes responsáveis pelo processamento assíncrono sejam escalados diretamente a partir da quantidade de mensagens presentes nas filas de processamento.

Essa abordagem aumenta a eficiência no uso dos recursos computacionais e torna a plataforma ainda mais aderente aos princípios de Event-Driven Architecture.