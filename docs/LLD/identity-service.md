# Identity Service LLD

## Objetivo

Implementar autenticacao, autorizacao e gerenciamento basico de usuarios da plataforma FIAP X Video Processing.

## Responsabilidades

- Registrar usuarios.
- Autenticar credenciais.
- Armazenar senhas com BCrypt.
- Emitir JWT.
- Disponibilizar informacoes do usuario autenticado quando necessario.

## Limites do Dominio

Pertence ao Identity Service:

- Usuario.
- Credencial.
- Token de acesso.
- Politicas de autenticacao.

Nao pertence ao Identity Service:

- Upload de videos.
- Status de processamento.
- Processamento de arquivos.
- Notificacoes de conclusao ou falha.

## Requisitos Atendidos

| Requisito | Atendimento |
|-----------|-------------|
| RF-01 | Autenticacao de usuarios. |
| RNF-04 | Seguranca de acesso e senhas. |
| RNF-08 | Testabilidade por camadas. |

## Casos de Uso

| Caso de uso | Descricao |
|-------------|-----------|
| RegisterUser | Cria usuario com email e senha criptografada. |
| AuthenticateUser | Valida credenciais e emite JWT. |
| GetAuthenticatedUser | Retorna dados basicos do usuario autenticado. |

## Arquitetura Interna

```mermaid
flowchart TB
  Controller["AuthController"]
  UseCases["RegisterUser / AuthenticateUser"]
  Domain["User / Email / PasswordHash"]
  RepoPort["UserRepositoryPort"]
  TokenPort["TokenProviderPort"]
  RepoAdapter["JpaUserRepositoryAdapter"]
  TokenAdapter["JwtTokenProviderAdapter"]

  Controller --> UseCases
  UseCases --> Domain
  UseCases --> RepoPort
  UseCases --> TokenPort
  RepoAdapter --> RepoPort
  TokenAdapter --> TokenPort
```

## Organizacao dos Pacotes

```text
br.com.fiapx.identity
  application.usecase
  application.port.in
  application.port.out
  domain.model
  domain.valueobject
  domain.exception
  infrastructure.adapter.in.web
  infrastructure.adapter.out.persistence
  infrastructure.adapter.out.security
  infrastructure.config
  shared.error
```

## Entidades

### User

| Campo | Tipo | Regra |
|-------|------|-------|
| id | UUID | Identificador do usuario. |
| name | String | Nome informado no cadastro. |
| email | Email | Unico no auth_db. |
| passwordHash | PasswordHash | Senha criptografada com BCrypt. |
| createdAt | Instant | Data de criacao. |

## Value Objects

| Value Object | Regra |
|--------------|-------|
| Email | Formato valido e normalizacao para comparacao. |
| PasswordHash | Representa senha ja criptografada, nunca senha pura. |

## DTOs

| DTO | Campos |
|-----|--------|
| RegisterUserRequest | name, email, password |
| UserResponse | id, name, email |
| LoginRequest | email, password |
| LoginResponse | accessToken, tokenType, expiresIn |

## Controllers

| Metodo | Endpoint | Uso |
|--------|----------|-----|
| POST | /api/auth/register | Cadastro de usuario. |
| POST | /api/auth/login | Autenticacao e emissao de JWT. |
| GET | /api/auth/me | Dados do usuario autenticado. |

## Use Cases

### RegisterUser

1. Validar entrada.
2. Verificar se email ja existe.
3. Criptografar senha com BCrypt.
4. Persistir usuario em auth_db.
5. Retornar dados publicos do usuario.

### AuthenticateUser

1. Buscar usuario por email.
2. Comparar senha com BCrypt.
3. Gerar JWT.
4. Retornar token.

## Ports

| Port | Direcao | Responsabilidade |
|------|---------|------------------|
| RegisterUserUseCase | Inbound | Cadastro de usuario. |
| AuthenticateUserUseCase | Inbound | Autenticacao. |
| UserRepositoryPort | Outbound | Persistencia de usuarios. |
| PasswordEncoderPort | Outbound | Hash e verificacao de senha. |
| TokenProviderPort | Outbound | Geracao e validacao de JWT. |

## Adapters

| Adapter | Tipo | Responsabilidade |
|---------|------|------------------|
| AuthController | Inbound HTTP | Expor autenticacao. |
| JpaUserRepositoryAdapter | Outbound persistence | Persistir usuarios. |
| BCryptPasswordEncoderAdapter | Outbound security | Hash de senha. |
| JwtTokenProviderAdapter | Outbound security | JWT. |

## Repositorios

| Repositorio | Banco | Operacoes |
|-------------|-------|-----------|
| UserRepository | auth_db | save, findByEmail, existsByEmail, findById |

## Eventos Publicados

Nenhum evento obrigatorio definido no HLD para o Identity Service.

## Eventos Consumidos

Nenhum evento obrigatorio definido no HLD para o Identity Service.

## Modelo de Dados

```mermaid
erDiagram
  users {
    uuid id PK
    string name
    string email UK
    string password_hash
    timestamp created_at
  }
```

## Fluxos

### Login

```mermaid
sequenceDiagram
  participant User
  participant Identity
  participant AuthDB

  User->>Identity: POST /api/auth/login
  Identity->>AuthDB: Busca usuario por email
  Identity->>Identity: Valida senha com BCrypt
  Identity-->>User: JWT
```

## Estrategia de Tratamento de Erros

| Erro | Resposta |
|------|----------|
| Email ja cadastrado | 409 CONFLICT |
| Credenciais invalidas | 401 UNAUTHORIZED |
| Requisicao invalida | 400 BAD REQUEST |

## Estrategia de Testes

- Unit tests para RegisterUser e AuthenticateUser.
- Unit tests para validacao de Email.
- Integration tests para persistencia com PostgreSQL via Testcontainers.
- Tests de seguranca para acesso sem token em `/api/auth/me`.

## Dependencias

- Spring Boot 3.x.
- Spring Security.
- BCrypt.
- PostgreSQL.
- Flyway.
- OpenTelemetry.

## Consideracoes

Refresh Token permanece opcional para o MVP e nao faz parte do escopo obrigatorio deste LLD.
