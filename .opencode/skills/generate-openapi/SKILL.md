---
name: generate-openapi
description: Generate OpenAPI spec from backend and prepare client generation.
---
## What I do
- Run `./mvnw -pl backend -Popenapi verify` to create `api/openapi.json`.
- Optionally, run `./mvnw -pl tui -Popenapi-client generate-sources` for WebClient client.
- Validate the generated spec.

## When to use me
Use when updating API endpoints or before sharing API docs with frontend teams.