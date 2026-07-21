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

---

# Notas de Hardening (Epic 015)

Checklist de segurança e operação revisado antes do release final. Cada item abaixo tem correção aplicada ou decisão registrada — nunca as duas coisas em aberto ao mesmo tempo.

- **Containers non-root**: os 4 Dockerfiles (`identity-service`, `video-service`, `processing-worker`, `notification-service`) criam e utilizam um usuário `fiapx` dedicado (`USER fiapx`), reduzindo a superfície de ataque em caso de comprometimento do processo da aplicação.
- **Validação de upload**: `video-service` valida content-type (allowlist `video/mp4`, `video/mpeg`, `video/quicktime`, `video/x-msvideo`, `video/x-matroska`) e tamanho máximo (100MB por arquivo, 105MB por requisição) antes de aceitar o vídeo, além da validação de arquivo não-vazio já existente. Ambos os limites são configuráveis via variável de ambiente (`VIDEO_UPLOAD_ALLOWED_CONTENT_TYPES`, `VIDEO_UPLOAD_MAX_FILE_SIZE_BYTES` e equivalentes do Spring multipart).
- **Fallbacks de `DB_PASSWORD`/`JWT_SECRET` em `application.yml`**: os valores default (`postgres`, `CHANGE_ME_DEV_ONLY_SECRET_KEY_MIN_32_BYTES_LONG_0000`) existem apenas para permitir a execução local sem configuração adicional. Em todo ambiente implantado (Docker Compose, Kubernetes), essas variáveis são sempre fornecidas pelo ambiente — Kubernetes Secret nos manifests Helm (`infrastructure/helm/cluster-setup`) — e o fallback nunca é de fato utilizado fora de dev/local.
- **CORS**: não configurado até o Epic 015. Como o projeto passou a incluir um frontend de demonstração (Epic 013) que fará chamadas cross-origin, `identity-service` e `video-service` passaram a expor uma política de CORS explícita (`fiapx.security.cors.allowed-origins`), com postura segura por padrão (nenhuma origem permitida até ser configurada via `IDENTITY_CORS_ALLOWED_ORIGINS`/`VIDEO_CORS_ALLOWED_ORIGINS`).
- **`LabRole` compartilhada (AWS Academy)**: restrição aceita do ambiente de laboratório, não um gap a corrigir. O ambiente do AWS Academy bloqueia a criação de IAM Roles e de um provedor OIDC (IRSA), então o cluster EKS e o node group reutilizam a `LabRole` provida pelo Academy (`infrastructure/terraform/iam.tf`), e os pods herdam credenciais AWS via instance profile do node, sem chaves estáticas em nenhum container.
- **S3**: bucket de vídeos já bloqueia todo acesso público (`aws_s3_bucket_public_access_block`, os 4 flags habilitados em `infrastructure/terraform/storage.tf`), e os downloads usam exclusivamente URLs pré-assinadas com expiração configurável. Nenhuma correção necessária — apenas registro de que o item já foi auditado e está correto.