# ADR-013 - Refresh Token e Logout no Escopo do MVP

## Status

Approved

## Contexto

`docs/LLD/identity-service.md` (Consideracoes) definia Refresh Token como opcional e fora do escopo obrigatorio do MVP, e nao mencionava Logout. ADR-009 ja registrava como trade-off que "revogacao de tokens exige estrategia futura".

## Problema

Sem refresh token, o unico jeito de renovar sessao expirada e um novo login, e sem logout nao ha como revogar uma sessao antes da expiracao natural do access token.

## Decisao

Trazer Refresh Token e Logout para o escopo obrigatorio do Identity Service (epic/003-identity), superando a nota de "opcional" em `docs/LLD/identity-service.md`.

Escopo minimo:
- `POST /api/auth/refresh`: troca um refresh token valido por um novo par access/refresh token (rotacao).
- `POST /api/auth/logout`: revoga um refresh token especifico (endpoint protegido por access token valido).
- Refresh token e um valor opaco (nao JWT), armazenado em `auth_db` apenas como hash (nunca em texto puro), com expiracao e flag `revoked`.

Fora de escopo (nao implementado por esta decisao): revogacao em massa de todas as sessoes de um usuario, listagem/gestao de sessoes ativas, blacklist de access tokens.

## Justificativa

Fecha a lacuna de revogacao ja sinalizada em ADR-009 com a menor superficie possivel, sem introduzir gestao de sessao multi-dispositivo nao solicitada.

## Consequencias

- `docs/LLD/identity-service.md` e `docs/api/openapi.yaml` sao atualizados para incluir os dois endpoints e o campo `refreshToken` em `LoginResponse`.
- Nova tabela `refresh_tokens` em `auth_db`.
- Access token continua stateless (JWT); apenas o refresh token e persistido e revogavel.

## Referencias

- docs/LLD/identity-service.md
- docs/ADR/ADR-009-security.md
- pendencies.md (TASK-002.6)
