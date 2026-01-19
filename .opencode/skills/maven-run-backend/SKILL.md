---
name: maven-run-backend
description: Start the backend Spring Boot server in dev profile for testing or development.
---
## What I do
- Execute `./mvnw -pl backend spring-boot:run -Dspring-boot.run.profiles=dev`.
- Ensure the server starts successfully and logs key endpoints.
- Stop gracefully after use.

## When to use me
Use for local testing of API endpoints or integration with TUI/Vaadin UI.