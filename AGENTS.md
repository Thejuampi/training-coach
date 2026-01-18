# AGENTS.md — training-coach

This guide is for agentic coding tools working in this repository. It captures build/test commands,
code style conventions, and the OpenCode agent role rules that apply here.

## Repository overview
- Stack: **Java 21 + Spring Boot 4.0.1**, multi-module Maven build.
- Use the **Maven Wrapper** (`./mvnw` / `mvnw.cmd`) to avoid Maven version drift.
- Architecture: **Imperative Shell / Functional Core** (hexagonal).
  - Ports live in `*/application/port`
  - Adapters live in `*/infrastructure`
  - Controllers live in `*/presentation`
  - Domain models in `*/domain/model` (simple, immutable where possible)
  - Orchestration in `*/application/service`

Modules (root `pom.xml`):
- `common`, `backend`, `tui`, `spring-shell`, `vaadin-ui`

Docs:
- Architecture overview: `docs/ARCHITECTURE.md`

## OpenCode configuration (from `opencode.jsonc`)
- Config file: `opencode.jsonc` at repo root.
- Default agent: **workflow-orchestrator**.
- Troubleshooting: run `opencode models` to list available models/providers.
  - Use valid provider/model combos (e.g., `github-copilot/gpt-5.2-codex`).
  - Avoid unsupported providers or unavailable model names.

### Agent roles and responsibilities
- **workflow-orchestrator** (default):
  - Orchestrates delivery and avoids routine clarifications from the human Originator.
  - Decision ladder:
    - Dev/Reviewer/Security questions → **Tech Lead**
    - Product intent ambiguity → **Product Owner**
    - Business decisions only → **Originator**
  - If blocked: make conservative assumptions and record them.

- **tech-lead**:
  - Converts validated use cases/scenarios into technical design + **dev-ready tickets**.
  - Mandatory delegation: spawn developer agents for implementation work.

- **team-lead**:
  - Coordinates PO/TL/Devs; must spawn developer agents when tickets are ready.

- **juan-backend-developer**:
  - Backend implementation, TDD-first. Blockers go to Tech Lead as RFIs.

- **maria-frontend-developer**:
  - Frontend implementation, TDD-first. Blockers go to Tech Lead as RFIs.

- **test-developer**:
  - Adds/extends unit/integration/BDD coverage based on acceptance criteria.

- **general**:
  - Consult-only (Q&A or critique). Must not own implementation work.

- **user-coach / user-athlete / user-admin**:
  - Simulated user personas for feedback; do not implement code.

## Build / test / lint

### Use the Maven wrapper
- macOS/Linux: `./mvnw`
- Windows: `mvnw.cmd`

The wrapper uses `.mvn/maven.config` and `.mvn/settings.xml` automatically.

### Build everything
```bash
./mvnw clean verify
# Windows
mvnw.cmd clean verify
```

### Build a single module
```bash
./mvnw -pl backend -am verify
./mvnw -pl tui -am verify
```

### Run the backend
```bash
./mvnw -pl backend spring-boot:run -Dspring-boot.run.profiles=dev
```

### Run all tests
```bash
./mvnw test
```

### Run tests for one module
```bash
./mvnw -pl backend test
./mvnw -pl tui test
```

### Run a single test class (Surefire)
```bash
./mvnw -pl backend -Dtest=AuthControllerTest test
./mvnw -pl backend -Dtest=*ServiceTest test
```

### Run a single test method
```bash
./mvnw -pl backend -Dtest=AuthControllerTest#loginReturnsUnauthorizedForInvalidCredentials test
```

### Integration-style tests (`*IT`)
- In `backend`, Surefire **excludes** `**/*IT.java` and `**/*IT.kt`.
- If you need to run an IT class explicitly, try:
```bash
./mvnw -pl backend -Dtest=UserControllerIT test
```
(If it is still excluded, coordinate with Tech Lead to add Failsafe.)

### Cucumber acceptance tests
- Features: `backend/src/test/resources/features/*.feature`
- Step defs (Kotlin): `backend/src/test/kotlin/com/training/coach/acceptance`
- Run via standard test task:
```bash
./mvnw -pl backend test
```

### Coverage
```bash
./mvnw test jacoco:report
./mvnw test jacoco:report jacoco:check
```

### Formatting / code quality
Spotless (Palantir Java Format) runs during `verify`.
```bash
./mvnw spotless:check
./mvnw spotless:apply
./mvnw spotbugs:check
./mvnw checkstyle:check
```

### OpenAPI generation
Backend profile `openapi` generates `api/openapi.json`:
```bash
./mvnw -pl backend -Popenapi verify
```
TUI profile `openapi-client` generates a WebClient client from `api/openapi.json`:
```bash
./mvnw -pl tui -Popenapi-client generate-sources
```

## Configuration & environment
- `.env.example` documents required environment variables.
- Application profiles live in:
  - `backend/src/main/resources/application.yml`
  - `backend/src/main/resources/application-dev.yml`
  - `backend/src/test/resources/application-test.yml`
- Intervals.icu config uses `intervals.icu.*` properties and requires an API key.

## Code style and conventions

### Architectural boundaries
- Domain layer should be pure, immutable, and free of framework annotations.
- Application services orchestrate, adapters perform IO.
- Controllers are thin; map HTTP to application services.

### Imports
- Prefer explicit imports (avoid `*`).
- Order: `java.*` → third-party → `com.training.coach.*`.

### Formatting
- Let Spotless/Palantir format handle layout.
- Do not hand-format; run `./mvnw spotless:apply` if needed.

### Types and immutability
- Prefer Java `record` for domain types.
- Validate invariants in record compact constructors (throw `IllegalArgumentException`).
- Prefer value objects in `shared/domain/unit/*` for units.

### Naming
- Classes: `PascalCase`, methods/fields: `camelCase`, constants: `UPPER_SNAKE_CASE`.
- Test classes: `*Test`. Integration tests: `*IT` (excluded from backend unit test run).

### Error handling
- Domain invariant violations: `IllegalArgumentException` or domain-specific exceptions.
- Functional core can use `com.training.coach.shared.functional.Result` for recoverable failures.
- API layer uses Spring `ResponseStatusException` for HTTP-mapped errors.

### Logging & security
- Do not log secrets (API keys, tokens, passwords).
- Keep logs concise; tests should remain quiet (test profile disables most logging).

## Testing patterns
- JUnit 5 is standard.
- WebFlux controllers may use `WebTestClient` (direct controller binding or Spring context).
- Cucumber tests use Spring + Kotlin step defs; reuse the existing
  `TestFitnessPlatformPort` in `CucumberSpringConfiguration.kt` when possible.

## Copilot/Cursor rules
- Copilot instructions live in `.github/copilot-instructions.md` and are included above.
- No Cursor rules (`.cursor/rules/` or `.cursorrules`) were found in this repo at time of writing.
