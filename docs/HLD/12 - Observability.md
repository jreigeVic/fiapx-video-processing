# 12 - Observability

## Objetivo

Este documento apresenta a estratégia de observabilidade adotada pela plataforma **FIAP X Video Processing**, descrevendo como métricas, logs e rastreamento distribuído são utilizados para monitorar a saúde da aplicação e facilitar a identificação de problemas operacionais.

A observabilidade é tratada como um requisito arquitetural desde o início do projeto, permitindo maior confiabilidade, facilidade de operação e suporte à evolução contínua da plataforma.

---

# Visão Geral

A plataforma foi projetada para fornecer visibilidade completa sobre o comportamento dos microsserviços durante todo o ciclo de execução.

A estratégia de observabilidade contempla três pilares fundamentais:

- Logs
- Métricas
- Traces Distribuídos

Esses pilares permitem compreender o comportamento da aplicação, identificar gargalos de desempenho e acelerar a resolução de incidentes.

---

# Logs

Os microsserviços registram eventos relevantes de execução, incluindo:

- inicialização dos serviços;
- requisições recebidas;
- eventos publicados e consumidos;
- erros de processamento;
- falhas de integração;
- exceções inesperadas.

Os logs são centralizados para facilitar consultas, auditorias e análise operacional.

---

# Métricas

A plataforma disponibiliza métricas que permitem acompanhar sua saúde operacional e utilização dos recursos.

Exemplos de indicadores monitorados:

- quantidade de requisições;
- tempo médio de resposta;
- quantidade de vídeos processados;
- taxa de falhas;
- utilização de recursos computacionais;
- quantidade de mensagens processadas.

Essas métricas apoiam decisões relacionadas à operação e escalabilidade da plataforma.

---

# Rastreamento Distribuído

A comunicação entre os microsserviços é rastreada por meio de tracing distribuído.

Essa estratégia permite acompanhar uma requisição durante todo o seu ciclo de vida, desde o envio do vídeo até a conclusão do processamento.

O rastreamento distribuído facilita:

- identificação de gargalos;
- análise de dependências;
- investigação de falhas;
- acompanhamento do fluxo completo da aplicação.

---

# Monitoramento da Infraestrutura

Além da aplicação, a infraestrutura também é monitorada continuamente.

Entre os recursos monitorados destacam-se:

- cluster Kubernetes;
- utilização dos containers;
- filas de processamento;
- armazenamento;
- banco de dados;
- disponibilidade dos serviços.

Essa abordagem permite identificar problemas tanto na aplicação quanto na infraestrutura subjacente.

---

# Estratégia Arquitetural

A arquitetura foi concebida para que todos os microsserviços emitam informações de observabilidade de maneira padronizada.

Essa padronização garante:

- consistência entre serviços;
- facilidade de monitoramento;
- menor esforço operacional;
- maior capacidade de diagnóstico.

---

# Ferramentas Adotadas

A solução utiliza as seguintes ferramentas para implementação da estratégia de observabilidade.

| Ferramenta | Objetivo |
|------------|----------|
| OpenTelemetry | Instrumentação da aplicação |
| New Relic | Observabilidade da aplicação (APM, Logs, Traces e Métricas) |
| Amazon CloudWatch | Monitoramento da infraestrutura e serviços AWS |

---

# Benefícios

A estratégia de observabilidade proporciona diversos benefícios operacionais, incluindo:

- redução do tempo de identificação de incidentes;
- maior confiabilidade operacional;
- monitoramento contínuo da plataforma;
- suporte à tomada de decisão baseada em métricas;
- facilidade para evolução da arquitetura.

---

# Considerações

A observabilidade constitui um dos principais atributos de qualidade da plataforma.

A estratégia apresentada neste documento fornece suporte às atividades de operação, manutenção e evolução da solução, permitindo maior confiabilidade durante todo o ciclo de vida da aplicação.

Os detalhes de instrumentação, configuração das ferramentas e dashboards serão apresentados posteriormente na documentação de Low Level Design.

## Alternativas Consideradas

Durante o processo de definição arquitetural foram avaliadas diferentes soluções para observabilidade.

Como estratégia principal foi adotada uma plataforma unificada de observabilidade baseada em OpenTelemetry e New Relic.

Soluções baseadas em Prometheus e Grafana permanecem como alternativas arquiteturais viáveis para futuras evoluções ou diferentes cenários de implantação.