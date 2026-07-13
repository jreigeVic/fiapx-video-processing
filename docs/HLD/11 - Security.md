# 11 - Security

## Objetivo

Este documento apresenta a estratégia de segurança adotada pela plataforma **FIAP X Video Processing**, descrevendo os princípios, mecanismos e controles utilizados para proteger usuários, dados e infraestrutura.

As decisões apresentadas têm como objetivo garantir confidencialidade, integridade e disponibilidade das informações ao longo de todo o ciclo de vida da aplicação.

---

# Princípios de Segurança

A arquitetura da plataforma foi concebida considerando segurança como um requisito transversal a todos os componentes da solução.

Os principais princípios adotados são:

- autenticação obrigatória para acesso aos recursos protegidos;
- princípio do menor privilégio (Least Privilege);
- isolamento entre microsserviços;
- proteção das credenciais da aplicação;
- comunicação segura entre os componentes;
- defesa em profundidade (Defense in Depth).

---

# Autenticação e Autorização

O acesso à plataforma é restrito a usuários autenticados.

Após a autenticação, o usuário recebe um token de acesso utilizado para autenticar as requisições aos serviços protegidos.

A autorização é realizada considerando o contexto do usuário autenticado e os recursos solicitados.

---

# Proteção dos Dados

A solução adota mecanismos para proteger informações sensíveis durante armazenamento e transmissão.

As principais estratégias incluem:

- comunicação protegida por HTTPS;
- armazenamento seguro de credenciais;
- criptografia de senhas;
- utilização de URLs temporárias para acesso aos arquivos armazenados.

---

# Segurança entre Microsserviços

A comunicação entre os componentes internos da plataforma ocorre em ambiente controlado, reduzindo a exposição dos serviços.

Além disso:

- cada serviço possui responsabilidades claramente definidas;
- os serviços acessam apenas os recursos necessários para seu funcionamento;
- a comunicação orientada a eventos reduz dependências diretas entre componentes.

---

# Gerenciamento de Credenciais

Credenciais e informações sensíveis não devem ser armazenadas no código da aplicação.

Todas as configurações sensíveis devem ser fornecidas pelo ambiente de execução através de mecanismos apropriados de gerenciamento de segredos.

---

# Segurança da Infraestrutura

A infraestrutura foi projetada para minimizar superfícies de ataque e restringir acessos desnecessários.

As principais estratégias incluem:

- isolamento dos serviços em ambiente Kubernetes;
- controle de acesso baseado em identidade;
- utilização de contas de serviço específicas para cada microsserviço;
- separação entre ambientes de execução.

---

# Segurança no Armazenamento

Os arquivos enviados pelos usuários permanecem armazenados em serviço dedicado de armazenamento de objetos.

O acesso aos arquivos ocorre apenas por meio dos serviços da plataforma, utilizando mecanismos temporários de autorização quando necessário.

---

# Auditoria

A plataforma registra eventos relevantes de autenticação, processamento e operação.

Esses registros permitem rastrear operações importantes e apoiar processos de auditoria e investigação de incidentes.

---

# Considerações

A estratégia de segurança apresentada neste documento estabelece as diretrizes gerais para proteção da plataforma.

Os detalhes de implementação relacionados à autenticação, autorização, gerenciamento de credenciais e configuração da infraestrutura serão apresentados posteriormente no Low Level Design e nas respectivas Architecture Decision Records (ADRs).