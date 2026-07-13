# Authentication API

## Objetivo

Documentar os endpoints HTTP do Identity Service para cadastro, login e consulta do usuario autenticado.

## Base Path

`/api/auth`

## Autenticacao

- `POST /register` e `POST /login` sao publicos.
- `GET /me` exige JWT valido no header `Authorization: Bearer <token>`.

## Endpoints

### POST /api/auth/register

Cria um usuario.

#### Request Body

```json
{
  "name": "User Name",
  "email": "user@example.com",
  "password": "plain-password"
}
```

#### Response 201

```json
{
  "id": "uuid",
  "name": "User Name",
  "email": "user@example.com"
}
```

#### Status Codes

| Status | Motivo |
|--------|--------|
| 201 | Usuario criado. |
| 400 | Entrada invalida. |
| 409 | Email ja cadastrado. |

### POST /api/auth/login

Autentica o usuario e retorna JWT.

#### Request Body

```json
{
  "email": "user@example.com",
  "password": "plain-password"
}
```

#### Response 200

```json
{
  "accessToken": "jwt",
  "tokenType": "Bearer",
  "expiresIn": 3600
}
```

#### Status Codes

| Status | Motivo |
|--------|--------|
| 200 | Autenticado. |
| 400 | Entrada invalida. |
| 401 | Credenciais invalidas. |

### GET /api/auth/me

Retorna dados do usuario autenticado.

#### Response 200

```json
{
  "id": "uuid",
  "name": "User Name",
  "email": "user@example.com"
}
```

#### Status Codes

| Status | Motivo |
|--------|--------|
| 200 | Usuario autenticado. |
| 401 | Token ausente ou invalido. |

## Fluxo

```mermaid
sequenceDiagram
  participant User
  participant IdentityService
  participant AuthDB

  User->>IdentityService: POST /api/auth/login
  IdentityService->>AuthDB: Busca usuario por email
  IdentityService->>IdentityService: Valida BCrypt
  IdentityService-->>User: JWT
```

## Erros Possiveis

Todas as respostas de erro devem seguir o contrato compartilhado definido em `docs/LLD/shared-architecture.md`.
