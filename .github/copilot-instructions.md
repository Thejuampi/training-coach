# Copilot instructions for training-coach

- Stack: Java 21 + Spring Boot (see parent in pom.xml) with Maven wrapper. Prefer `mvnw`/`mvnw.cmd` so local Maven versions don’t drift.
- Architecture: Imperative Shell / Functional Core (hexagonal). Ports live under `application/port`, adapters under `infrastructure` (example port: `FitnessPlatformPort`, adapter: `IntervalsIcuAdapter`). Controllers are in `*/presentation` packages.
- Domain model lives in `*/domain/model` and is kept simple/immutable where possible. Application services orchestrate logic under `*/application/service`.
- Integration: Intervals.icu via WebClient + Jackson `JsonNode` in the adapter. Configuration uses `intervals.icu.*` properties and requires an API key (see `IntervalsIcuAdapter` and `IntervalsIcuConnectivityTest`).
- Configuration profiles live in `src/main/resources/application*.yml` and `src/test/resources/application-test.yml`.
- Testing: JUnit 5 unit tests in `src/test/java`, Cucumber acceptance tests in `src/test/kotlin/com/training/coach/acceptance` with Gherkin features in `src/test/resources/features`.
- Code style: Spotless (Palantir Java Format) runs in `verify`. If formatting fails, run `mvn spotless:apply` before re-running the build.

References:
- README: build/test commands and environment variables.
- Architecture overview: `docs/ARCHITECTURE.md`.
- Ports/adapters example: `src/main/java/com/training/coach/athlete/application/port/out/FitnessPlatformPort.java` and `src/main/java/com/training/coach/athlete/infrastructure/adapter/IntervalsIcuAdapter.java`.
