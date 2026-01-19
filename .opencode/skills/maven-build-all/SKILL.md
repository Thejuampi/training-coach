---
name: maven-build-all
description: Run a full Maven build and verification for all modules, including tests and quality checks.
---
## What I do
- Execute `./mvnw clean verify` to build, test, and lint all modules.
- Ensure Spotless formatting, SpotBugs, and Checkstyle pass.
- Fail if any issues (e.g., test failures, formatting errors).

## When to use me
Use before committing changes or after major refactors to ensure the project is clean and buildable.