# 04 - Requisitos Não Funcionais

## Objetivo

Este documento apresenta os requisitos não funcionais da plataforma **FIAP X Video Processing**.

Enquanto os requisitos funcionais descrevem **o que** a solução deve realizar, os requisitos não funcionais definem **como** a solução deve se comportar em termos de qualidade, desempenho, segurança, disponibilidade e operação.

Os requisitos apresentados neste documento servem como direcionadores das decisões arquiteturais descritas ao longo deste High Level Design.

---

# RNF-01 – Escalabilidade

A solução deve suportar crescimento horizontal de seus componentes sem necessidade de alterações na lógica de negócio.

## Critérios

- Os serviços devem ser stateless sempre que possível.
- O processamento deve permitir múltiplas instâncias simultâneas.
- A plataforma deve suportar expansão automática da capacidade computacional.

---

# RNF-02 – Disponibilidade

A plataforma deve permanecer disponível mesmo diante de falhas individuais de componentes.

## Critérios

- Os serviços devem permitir múltiplas réplicas.
- Falhas isoladas não devem interromper completamente a operação da plataforma.
- Componentes críticos devem ser executados em infraestrutura gerenciada.

---

# RNF-03 – Resiliência

Falhas durante o processamento não devem resultar em perda de solicitações ou inconsistência dos dados.

## Critérios

- O processamento deve tolerar falhas temporárias.
- Operações críticas devem ser idempotentes.
- Mensagens não processadas devem permanecer disponíveis para nova tentativa.

---

# RNF-04 – Segurança

A plataforma deve proteger dados, usuários e recursos contra acessos não autorizados.

## Critérios

- Apenas usuários autenticados poderão acessar recursos protegidos.
- Dados sensíveis devem permanecer protegidos durante armazenamento e transporte.
- Credenciais nunca devem ser armazenadas diretamente no código da aplicação.

---

# RNF-05 – Observabilidade

A solução deve fornecer mecanismos que permitam monitorar sua saúde operacional.

## Critérios

- Deve ser possível acompanhar métricas da aplicação.
- Deve ser possível rastrear requisições entre os serviços.
- Eventos de erro devem ser registrados para análise posterior.

---

# RNF-06 – Performance

O processamento de vídeos não deve bloquear a interação do usuário com a plataforma.

## Critérios

- Solicitações de upload devem responder rapidamente.
- O processamento deverá ocorrer de forma assíncrona.
- Operações demoradas devem ser desacopladas da experiência do usuário.

---

# RNF-07 – Manutenibilidade

A arquitetura deve facilitar manutenção, evolução e substituição de componentes.

## Critérios

- Serviços devem possuir responsabilidades bem definidas.
- Componentes devem apresentar baixo acoplamento.
- Alterações em um domínio devem minimizar impactos sobre os demais.

---

# RNF-08 – Testabilidade

A solução deve possibilitar validação automatizada dos principais fluxos de negócio.

## Critérios

- Componentes devem permitir testes unitários.
- Fluxos críticos devem possuir testes de integração.
- A arquitetura deve favorecer isolamento entre dependências.

---

# RNF-09 – Portabilidade

A solução deve poder ser executada de maneira consistente em diferentes ambientes.

## Critérios

- O processo de implantação deve ser reproduzível.
- Configurações de ambiente devem permanecer externas ao código.
- A infraestrutura deve ser descrita como código.

---

# RNF-10 – Entregabilidade

A plataforma deve permitir entregas frequentes, previsíveis e automatizadas.

## Critérios

- O processo de build deve ser automatizado.
- A publicação da aplicação deve ser reproduzível.
- O deploy deve minimizar indisponibilidades.

---

# Rastreabilidade

Os requisitos não funcionais apresentados neste documento fundamentam as decisões arquiteturais descritas nas próximas seções deste High Level Design, especialmente:

- Atributos de Qualidade;
- Arquitetura da Solução;
- Estratégia de Segurança;
- Estratégia de Observabilidade;
- Estratégia de Escalabilidade;
- Estratégia de Deploy.