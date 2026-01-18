# Training Coach

AI-powered endurance training coach with Seiler framework for polarized training.

## Overview

This application provides intelligent training plan generation based on Stephen Seiler's polarized training methodology (80/20 distribution), with AI integration for personalized coaching recommendations.

## Technology Stack

- **Java 21** with Spring Boot 4.0.1
- **Spring Data JPA** with H2
- **JUnit 5** (Kotlin tests) for TDD
- **MapStruct** for object mapping
- **Lombok** for reducing boilerplate
- **Intervals.icu API** for fitness platform integration
- **Claude API** for AI-powered coaching
- **H2 Database** for embedded storage

## Architecture

The project follows **Imperative Shell / Functional Core** pattern:

- **Domain Layer**: Pure functions and immutable records for business logic
- **Application Layer**: Orchestration with dependency injection
- **Infrastructure Layer**: IO operations, external APIs, JPA entities (mutable)
- **Interface Layer**: REST API controllers

## Project Structure

```
training-coach/
├── athlete/              # Athlete management feature
├── trainingplan/         # Training plan generation feature
├── workout/              # Workout synchronization feature
├── sync/                 # External platform sync feature
├── analysis/             # Performance analysis feature
├── feedback/             # Athlete feedback feature
├── integration/          # Plugin integrations (ChatGPT, Claude)
└── shared/              # Shared utilities and configurations
```

## Setup

### Prerequisites

- Java 21 or higher
- Maven 3.9+
- H2 (embedded, no external database required)
- Git

### Environment Variables

Copy `.env.example` to `.env` and configure your API keys:

```bash
cp .env.example .env
```

Edit `.env` with your actual values:

```bash
# Intervals.icu Integration
INTERVALS_ICU_API_KEY=your_actual_api_key

# Claude API Integration
CLAUDE_API_KEY=your_actual_claude_api_key

# Database Configuration
SPRING_DATASOURCE_URL=jdbc:h2:file:./data/training_coach;DB_CLOSE_DELAY=-1;AUTO_SERVER=TRUE
SPRING_DATASOURCE_USERNAME=sa
SPRING_DATASOURCE_PASSWORD=
```

### Database Setup

No external database setup is required. H2 will create the database file on first run.

### Build and Run

Gradle is disabled for this project; use Maven.

```bash
# Build the project
mvn clean install

# Run the application (dev profile)
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Run tests with TDD
mvn test

# Run with coverage report
mvn test jacoco:report

# Run aggregated coverage report (HTML at target/site/jacoco-aggregate/index.html)
./mvnw verify

```

### Code Quality

```bash
# Check code formatting (Spotless)
mvn spotless:check

# Auto-format code
mvn spotless:apply

# Run SpotBugs analysis
mvn spotbugs:check

# Run Checkstyle
mvn checkstyle:check
```

## Development Workflow

1. Write a failing test (TDD Red)
2. Implement minimal code to make test pass (TDD Green)
3. Refactor while keeping tests green (TDD Refactor)
4. Ensure coverage meets thresholds (80% line, 70% branch)
5. Verify code quality (Spotless, SpotBugs, Checkstyle)

## API Documentation

Once the application is running, access API documentation at:

```
http://localhost:8080/swagger-ui/index.html
```

## Security (Dev Defaults)

Spring Security basic auth is enabled with database-backed users.

On first run, a bootstrap admin is created if missing:

- Username: `admin` (or `SECURITY_BOOTSTRAP_ADMIN_USERNAME`)
- Password: `adminpass` (or `SECURITY_BOOTSTRAP_ADMIN_PASSWORD`)

## Feature Documentation

- `docs/FEATURES.md`
- `docs/USE_CASES.md`
- `docs/UI_VIEWS.md`
- `docs/SEILER_INTENSITY_MODEL.md`
- `docs/GLOSSARY.md`

## Contributing

This project follows strict TDD and code quality standards:

- **Test Coverage**: Minimum 80% line, 70% branch coverage
- **Code Style**: Enforced by Spotless (Palantir Java Format)
- **Code Quality**: Zero high-priority SpotBugs issues
- **Testing**: JUnit 5 with Kotlin tests for comprehensive scenarios

## License

Proprietary - All rights reserved
