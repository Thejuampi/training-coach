# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build System and Commands

This project uses **Maven 3.9+** with Maven wrapper (`mvnw`/`mvnw.cmd`). Gradle is disabled.

### Essential Commands
```bash
# Build and run
./mvnw clean install                      # Full build with tests
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev  # Run backend (dev profile)

# Testing
./mvnw test                              # Run all tests (parallel execution)
./mvnw -pl backend -Dtest=AuthControllerTest test  # Run single test class
./mvnw -pl backend -Dtest=CucumberTest test  # Run Cucumber acceptance tests
./mvnw test jacoco:report                # Run tests with coverage
./mvnw verify                             # Aggregated coverage report (HTML at target/site/jacoco-aggregate/index.html)

# Code quality
./mvnw spotless:check                     # Check code formatting
./mvnw spotless:apply                     # Auto-format code
./mvnw spotbugs:check                     # Run SpotBugs analysis
./mvnw checkstyle:check                   # Run Checkstyle
```

### Testing Architecture
- **Parallel execution**: 4 threads for blazing fast test feedback
- **Test isolation**: In-memory repositories with `@ScenarioScope` for Cucumber
- **Coverage requirements**: 80% line, 70% branch coverage minimum
- **Cucumber features**: 17 feature files covering all major scenarios

## Architecture Overview

**Imperative Shell / Functional Core** pattern with hexagonal design:

- **Domain Layer**: Pure functions and immutable records for business logic
- **Application Layer**: Orchestration with dependency injection
- **Infrastructure Layer**: IO operations, external APIs, JPA entities (mutable)
- **Interface Layer**: REST API controllers in `*/presentation` packages

### Key Design Patterns
- **Ports & Adapters**: Ports under `application/port`, adapters under `infrastructure`
  - Example: `FitnessPlatformPort` and `IntervalsIcuAdapter`
- **Clean Architecture**: Domain models simple/immutable where possible
- **Application Services**: Orchestration under `*/application/service`

### Bounded Contexts
- **Athlete**: Profile, metrics (FTP/FTHR/VO2), preferences, readiness
- **Training Plan**: Polarized (Seiler 80/20) plan generation, compliance tracking
- **Sync**: Platform adapters (Intervals.icu first), external data ingestion
- **Analysis/Readiness**: Load trends, recovery flags, readiness scoring
- **Coaching Assist**: Explanations and suggestions (deterministic rules govern safety)

## Technology Stack

### Core Technologies
- **Java 21** with Spring Boot 4.0.1 and WebFlux
- **Spring Data JPA** with H2 in-memory database
- **Spring Security** with OAuth2 and JWT tokens
- **Spring Cache** with Caffeine
- **JUnit 5** with Kotlin tests

### Key Dependencies
- **MapStruct** for object mapping
- **Lombok** for reducing boilerplate
- **OpenAPI** for API documentation
- **Jackson** for JSON processing with `JsonNode`

### External Integrations
- **Intervals.icu API**: Fitness platform integration (WebClient + Jackson)
- **OAuth2**: Resource server configuration

## Configuration and Environment

### Application Configuration
- Main config: `src/main/resources/application.yml` (dev profile active)
- Test config: `src/test/resources/application-test.yml`
- Environment variables: Copy `.env.example` to `.env`

### Environment Variables
```bash
# Required for integrations
INTERVALS_ICU_API_KEY=your_api_key
CLAUDE_API_KEY=your_claude_api_key

# Database (H2 embedded, defaults usually sufficient)
SPRING_DATASOURCE_URL=jdbc:h2:file:./data/training_coach;DB_CLOSE_DELAY=-1;AUTO_SERVER=TRUE
SPRING_DATASOURCE_USERNAME=sa
SPRING_DATASOURCE_PASSWORD=
```

### Security (Dev Defaults)
- Basic auth with database-backed users
- Bootstrap admin on first run:
  - Username: `admin` (or `SECURITY_BOOTSTRAP_ADMIN_USERNAME`)
  - Password: `adminpass` (or `SECURITY_BOOTSTRAP_ADMIN_PASSWORD`)

## Development Workflow

### TDD Approach
1. Write failing test (Red)
2. Implement minimal code to pass test (Green)
3. Refactor while keeping tests green

### Commit Messages
- NEVER mention AI, Claude, or any AI assistance in commit messages
- Focus on the technical implementation and business value
- Examples:
  - ✅ "feat: Add GDPR-compliant athlete data deletion"
  - ✅ "fix: Handle partial sync failures gracefully"
  - ❌ "feat: Implement athlete deletion with AI assistance"
  - ❌ "refactor: Improve code with Claude suggestions"

### Test Organization
- **Unit tests**: JUnit 5 in `src/test/java`, focus on domain logic
- **Integration tests**: Spring context tests
- **Acceptance tests**: Cucumber BDD in `src/test/kotlin/com/training/coach/acceptance`
- **Test configs**: Dedicated test-only configurations in `backend/src/test/java/com/training/coach/testconfig/`

### Code Quality Requirements
- **Spotless**: Palantir Java Format (enforced in verify)
- **SpotBugs**: Zero high-priority issues
- **Checkstyle**: Enforced code style
- **Coverage**: 80% line, 70% branch minimum

## Project Structure

```
backend/src/main/java/com/training/coach/
├── application/     # Application services and ports
├── domain/         # Domain models and business logic
├── infrastructure/# External adapters and persistence
├── presentation/  # REST API controllers
└── shared/        # Shared utilities
```

### Module Structure
- **common/**: Shared utilities and configurations
- **backend/**: Spring Boot backend (main application)
- **tui/**: Terminal UI (Lanterna-based)
- **spring-shell/**: CLI interface
- **vaadin-ui/**: Web UI
- **api/**: OpenAPI specifications

## Key Implementation Details

### Cucumber Tests
- Run concurrently with thread-safe design
- `@ScenarioScope` prevents shared field state across scenarios
- In-memory repositories for test isolation
- No external IO - all ports run in-memory

### Domain Constraints
- **Seiler Polarized Training**: 80/20 distribution enforced
- **AI Usage**: Advisory only - deterministic rules enforce safety
- **Load Management**: Daily intensity caps, weekly volume adjustments
- **Readiness Scoring**: Combines objective (HRV, RHR, load) and subjective (RPE, soreness) metrics

### External API Integration
- **Intervals.icu**: First-class adapter with WebClient
- **Configuration**: `intervals.icu.*` properties require API key
- **Error Handling**: Robust retry logic and graceful degradation

## Architecture Documentation

See `docs/ARCHITECTURE.md` for detailed architecture diagrams and domain model relationships.