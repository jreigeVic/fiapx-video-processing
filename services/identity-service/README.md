# Identity Service

Authenticates users and manages sessions for the FIAP X Video Processing Platform: registration, login, refresh/rotation, logout and the current-user lookup (`GET /me`).

## Responsibilities

- `POST /api/auth/register`, `/login`, `/refresh`, `/logout`, `GET /me` - see `docs/api/authentication.md` and `docs/api/openapi.yaml`.
- Password hashing with BCrypt; access tokens as JWT (stateless); refresh tokens as opaque, hashed values with expiration and revocation (`ADR-013`).
- Publishes no events - the only producer/consumer of `auth_db`.

## Architecture

Java 21, Spring Boot 3.3, Gradle (Kotlin DSL), Flyway, PostgreSQL. Hexagonal/Clean Architecture (`com.fiapx.identity.{api,application,domain,infrastructure,configuration}`), enforced by `src/test/.../architecture/HexagonalArchitectureTest.java` (ArchUnit). See `docs/LLD/identity-service.md` for the full design.

## Configuration

Key environment variables (defaults in `application.yml`): `DB_USER`, `DB_PASSWORD`, `JWT_SECRET`, `JWT_ACCESS_TOKEN_TTL_SECONDS`, `JWT_REFRESH_TOKEN_TTL_SECONDS`, `IDENTITY_CORS_ALLOWED_ORIGINS`, `PORT` (default `8081`).

## Build, run, test

```bash
./gradlew build      # build, unit + integration tests, JaCoCo, ArchUnit
./gradlew bootRun
./gradlew test
docker build -t identity-service:0.1.0 .
```

Integration tests requiring Postgres use Testcontainers (needs a local Docker daemon). Live Swagger UI: `/swagger-ui/index.html`.
