---
name: maven-run-tests
description: Execute all unit and integration tests, including Cucumber acceptance tests.
---
## What I do
- Run `./mvnw test` for JUnit 5 unit tests and Cucumber (Kotlin step defs).
- Include integration tests (`*IT`) if explicitly enabled.
- Generate coverage reports with Jacoco.

## When to use me
Use after code changes to validate functionality, or for CI-like validation.