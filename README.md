# Code Quality Assessment Platform

Production-grade Spring Boot 3 microservice that registers GitHub repositories,
scans their source with an AI model, detects bugs, generates unit tests, scores
quality, and assesses compliance — exposed through secured REST APIs.

## Tech stack
Java 21 · Spring Boot 3.3 · Spring Security (JWT) · Spring Data JPA · PostgreSQL ·
Flyway · Lombok · MapStruct · springdoc OpenAPI · Maven

## Architecture
Clean, layered design:

```
web (controllers)  ->  service (business logic)  ->  repository (JPA)  ->  domain (entities)
                         |                 |
                  service.github    service.ai / service.scoring
```

- `config` — security, OpenAPI, WebClient, JPA auditing, typed `@ConfigurationProperties`
- `security` — stateless JWT filter, `JwtService`, `UserDetailsService`
- `domain` — JPA entities and enums
- `dto` + `mapper` — request/response records mapped with MapStruct
- `service` — `Auth`, `Repository`, `Scan` (+ async `ScanExecutor`), `Dashboard`,
  `github.GitHubService`, `ai.AiAnalysisService`, `scoring.QualityScoringService`
- `exception` — `@RestControllerAdvice` global handler with structured `ApiError`

## Run

1. Start PostgreSQL and create the database:
   ```bash
   createdb codequality
   ```
2. Copy `.env.example` to `.env`, set `AI_API_KEY` and `GITHUB_TOKEN`, and export the vars.
3. Build and run (requires Maven 3.9+):
   ```bash
   mvn spring-boot:run
   ```
   Flyway applies the schema on startup.

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Health: `http://localhost:8080/actuator/health`

## API overview
| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/v1/auth/register` | Register, returns JWTs |
| POST | `/api/v1/auth/login` | Login, returns JWTs |
| POST | `/api/v1/repositories` | Register a repository |
| GET | `/api/v1/repositories` | List repositories |
| GET | `/api/v1/repositories/{id}` | Get a repository |
| DELETE | `/api/v1/repositories/{id}` | Deactivate a repository |
| POST | `/api/v1/repositories/{id}/scans` | Trigger a scan (async, `202`) |
| GET | `/api/v1/repositories/{id}/scans` | Scan history (paged) |
| GET | `/api/v1/scans/{id}` | Full scan results |
| GET | `/api/v1/dashboard/metrics` | Aggregated metrics |

All endpoints except `/auth/**`, swagger, and health require a
`Authorization: Bearer <token>` header.

## Scan flow
`POST /repositories/{id}/scans` persists a `PENDING` scan and, after commit,
runs asynchronously: resolve HEAD commit → fetch analyzable files from GitHub →
per-file AI analysis (issues, tests, compliance, quality dimensions) → aggregate
into a deterministic `QualityScore` → mark `COMPLETED` (or `FAILED` with an error).

## Tests
```bash
mvn test
```
Context load runs against in-memory H2; `QualityScoringServiceTest` covers the
scoring algorithm.
```
