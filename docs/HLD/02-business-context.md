# 02 - Contexto de Negócio

## Objetivo

Este documento apresenta o contexto de negócio da plataforma **FIAP X Video Processing**, descrevendo o problema que motivou a criação da solução, os objetivos de negócio, os principais atores envolvidos e o fluxo funcional esperado.

Seu propósito é fornecer uma visão de alto nível do domínio antes da apresentação das decisões arquiteturais descritas nos documentos subsequentes do High Level Design.

---

# Contexto do Negócio

O crescimento do volume de conteúdo audiovisual e a necessidade de processamento cada vez mais rápido exigem plataformas capazes de executar operações intensivas de forma escalável e resiliente.

Nesse cenário, a FIAP X necessita modernizar sua plataforma de processamento de vídeos, substituindo uma arquitetura monolítica por uma solução baseada em microsserviços e computação em nuvem.

A nova plataforma deve permitir que usuários autenticados enviem vídeos para processamento, acompanhem sua execução e obtenham o resultado gerado, garantindo uma experiência consistente mesmo sob altas cargas de utilização.

Além de atender às necessidades atuais, a solução deve permitir evolução contínua, suportando novos consumidores de eventos e futuras funcionalidades sem impacto significativo nos serviços existentes.

---

# Desafios de Negócio

A solução atual apresenta limitações que dificultam sua evolução e comprometem sua capacidade de crescimento.

Os principais desafios identificados são:

- processamento síncrono que aumenta o tempo de espera do usuário;
- dificuldade para escalar conforme o aumento da demanda;
- elevado acoplamento entre componentes da aplicação;
- maior impacto operacional em caso de falhas durante o processamento;
- baixa flexibilidade para incorporar novas funcionalidades ou integrações.

Esses desafios justificam a adoção de uma arquitetura distribuída baseada em eventos e serviços independentes.

---

# Objetivos de Negócio

A nova plataforma foi concebida para atender aos seguintes objetivos estratégicos:

- proporcionar melhor experiência para o usuário durante o envio e acompanhamento dos vídeos;
- permitir processamento paralelo de múltiplos vídeos;
- garantir maior disponibilidade da solução;
- possibilitar evolução independente dos componentes da plataforma;
- reduzir impactos causados por falhas durante o processamento;
- aumentar a capacidade de crescimento da solução sem necessidade de grandes mudanças arquiteturais.

---

# Atores

## Usuário

Pessoa autenticada responsável por:

- realizar autenticação na plataforma;
- enviar vídeos para processamento;
- acompanhar o status das solicitações;
- realizar o download do resultado processado.

---

## Plataforma FIAP X

Sistema responsável por:

- receber solicitações dos usuários;
- armazenar os arquivos enviados;
- coordenar o processamento assíncrono;
- disponibilizar informações de acompanhamento;
- entregar o resultado final ao usuário.

---

# Capacidades da Plataforma

A plataforma oferece as seguintes capacidades de negócio:

- autenticação de usuários;
- gerenciamento de vídeos enviados;
- processamento assíncrono;
- acompanhamento do ciclo de processamento;
- disponibilização do resultado para download;
- comunicação desacoplada entre componentes por meio de eventos.

Essas capacidades foram organizadas de forma que cada domínio possa evoluir independentemente ao longo do ciclo de vida da aplicação.

---

# Fluxo de Negócio

```mermaid
flowchart LR

User["Usuário"]

Authentication["Autenticação"]

Upload["Upload do Vídeo"]

Processing["Processamento"]

Status["Consulta de Status"]

Download["Download do Resultado"]

User --> Authentication
Authentication --> Upload
Upload --> Processing
Processing --> Status
Status --> Download
```

---

# Benefícios Esperados

A arquitetura proposta busca gerar benefícios tanto para o negócio quanto para a operação da plataforma.

## Benefícios para o negócio

- melhor experiência do usuário;
- redução do tempo de espera percebido;
- maior capacidade de atendimento simultâneo;
- facilidade para evolução da plataforma.

## Benefícios técnicos

- baixo acoplamento entre serviços;
- escalabilidade horizontal;
- maior disponibilidade;
- maior resiliência operacional;
- observabilidade da solução;
- facilidade de manutenção e evolução contínua.

---

# Escopo da Solução

O escopo do projeto contempla:

- autenticação de usuários;
- envio de vídeos;
- armazenamento dos arquivos;
- processamento assíncrono;
- consulta do status do processamento;
- disponibilização do resultado para download;
- monitoramento da plataforma;
- automação da infraestrutura e do processo de entrega.

Não fazem parte do escopo do MVP funcionalidades relacionadas a analytics, auditoria avançada, novos consumidores especializados de eventos ou recursos adicionais de gerenciamento administrativo.

---

# Considerações

O contexto apresentado neste documento fundamenta as decisões arquiteturais descritas nas próximas seções do High Level Design.

Os requisitos funcionais e não funcionais apresentados posteriormente derivam diretamente dos objetivos de negócio descritos neste documento.