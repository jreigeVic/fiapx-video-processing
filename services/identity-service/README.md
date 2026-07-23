# 🔐 Identity Service

Autentica usuários e gerencia sessões para a Plataforma FIAP X de Processamento de Vídeo: registro, login, refresh/rotação, logout e consulta do usuário atual (`GET /me`).

## 📋 Responsabilidades

- `POST /api/auth/register`, `/login`, `/refresh`, `/logout`, `GET /me` - veja [`docs/api/authentication.md`](../../docs/api/authentication.md) e [`docs/api/openapi.yaml`](../../docs/api/openapi.yaml).
- Hash de senha com BCrypt; access tokens como JWT (stateless); refresh tokens como valores opacos e hasheados, com expiração e revogação ([ADR-013](../../docs/ADR/ADR-013-identity-refresh-logout-scope.md)).
- Não publica eventos - é o único produtor/consumidor de `auth_db`.

## 🏗️ Arquitetura

Java 21, Spring Boot 3.3, Gradle (Kotlin DSL), Flyway, PostgreSQL. Clean/Hexagonal Architecture (`com.fiapx.identity.{api,application,domain,infrastructure,configuration}`), reforçada por `src/test/.../architecture/HexagonalArchitectureTest.java` (ArchUnit). Veja [`docs/LLD/identity-service.md`](../../docs/LLD/identity-service.md) para o design completo.

## ⚙️ Configuração

Principais variáveis de ambiente (valores padrão em `application.yml`): `DB_USER`, `DB_PASSWORD`, `JWT_SECRET`, `JWT_ACCESS_TOKEN_TTL_SECONDS`, `JWT_REFRESH_TOKEN_TTL_SECONDS`, `IDENTITY_CORS_ALLOWED_ORIGINS`, `PORT` (padrão `8081`).

## ▶️ Build, execução e testes

```bash
./gradlew build      # build, testes unitários + integração, JaCoCo, ArchUnit
./gradlew bootRun
./gradlew test
docker build -t identity-service:0.1.0 .
```

Testes de integração que precisam de Postgres usam Testcontainers (exige um daemon Docker local). Swagger UI ao vivo: `/swagger-ui/index.html`.
