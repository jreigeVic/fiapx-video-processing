# 03 - Functional Requirements

## Objetivo

Este documento descreve os requisitos funcionais da plataforma FIAP X Video Processing.

Os requisitos apresentados representam as capacidades funcionais esperadas pela solução e servem como base para o detalhamento arquitetural e para a implementação dos microsserviços.

Cada requisito poderá ser rastreado posteriormente até os componentes responsáveis por sua implementação durante o Low Level Design (LLD).

---

# Requisitos Funcionais

## RF-01 – Autenticação de Usuários

A plataforma deve permitir que usuários autenticados acessem as funcionalidades disponibilizadas pelo sistema.

### Critério de aceite

- O usuário deve conseguir realizar autenticação utilizando credenciais válidas.
- Apenas usuários autenticados podem acessar recursos protegidos.

---

## RF-02 – Upload de Vídeos

A plataforma deve permitir o envio de vídeos para processamento.

### Critério de aceite

- O usuário deve conseguir enviar um ou mais arquivos de vídeo.
- O sistema deve validar a solicitação antes de iniciar o processamento.

---

## RF-03 – Armazenamento dos Arquivos

Após o envio, o sistema deve armazenar os arquivos enviados em um repositório persistente.

### Critério de aceite

- O arquivo enviado deve permanecer disponível durante todo o ciclo de processamento.

---

## RF-04 – Processamento Assíncrono

O processamento do vídeo deve ocorrer de forma assíncrona.

O usuário não deve permanecer aguardando a conclusão do processamento.

### Critério de aceite

- O envio do vídeo deve retornar imediatamente após o recebimento da solicitação.
- O processamento deverá ocorrer posteriormente.

---

## RF-05 – Acompanhamento do Processamento

O usuário deve conseguir acompanhar o status de processamento do vídeo.

### Critério de aceite

Os seguintes estados devem ser suportados:

- Recebido
- Em processamento
- Processado
- Falha

---

## RF-06 – Download do Resultado

Após o término do processamento, o usuário deve conseguir realizar o download do resultado produzido pela plataforma.

### Critério de aceite

- O download somente poderá ocorrer após a conclusão do processamento.

---

## RF-07 – Notificações

A plataforma deve permitir a emissão de notificações relacionadas ao processamento.

### Critério de aceite

- O sistema deve ser capaz de informar a conclusão ou falha do processamento.

---

## RF-08 – Publicação de Eventos

A plataforma deve publicar eventos representando mudanças relevantes de estado durante o ciclo de vida do processamento.

### Critério de aceite

Eventos devem representar as principais transições de estado do processamento.

---

## RF-09 – Processamento Paralelo

A plataforma deve suportar múltiplos vídeos sendo processados simultaneamente.

### Critério de aceite

O processamento de um vídeo não deve bloquear o processamento dos demais.

---

## RF-10 – Consulta de Histórico

A plataforma deve permitir consultar informações sobre vídeos previamente enviados.

### Critério de aceite

O usuário deve conseguir recuperar o histórico de processamento dos vídeos enviados.

---

# Rastreabilidade

Os requisitos funcionais apresentados neste documento serão detalhados posteriormente durante o Low Level Design.

Cada microsserviço deverá indicar explicitamente quais requisitos funcionais atende.

Essa rastreabilidade facilita futuras evoluções da solução e reduz o impacto de mudanças arquiteturais.