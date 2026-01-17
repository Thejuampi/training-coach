# Data Contracts (Draft)

This document captures API-facing data contracts and invariants used by UI and tests. It is intentionally concise and aligned to the acceptance scenarios.

## Authentication

- `POST /api/auth/login`
  - Request: `{ "username": string, "password": string }`
  - Response: `{ "accessToken": string, "refreshToken": string, "expiresInSeconds": number }`
- `POST /api/auth/refresh`
  - Request: `{ "refreshToken": string }`
  - Response: same as login
- `POST /api/auth/logout`
  - Request: `{ "refreshToken": string, "allSessions": boolean }`
  - Response: `204 No Content`

## Users

- `POST /api/users`
  - Request: `{ "name": string, "role": "ADMIN"|"COACH"|"ATHLETE", "preferences": UserPreferences, "username": string, "password": string }`
  - Response: `SystemUser`
- `GET /api/users/{id}/credentials`
  - Response: `{ "enabled": boolean, "lastRotatedAt": string|null }` (no secrets, no hashes)

## Athletes

- `POST /api/athletes`
  - Request: `Athlete`
  - Invariant: metrics submitted on create may be overridden by server defaults.
- `PUT /api/athletes/{id}`
  - Request: `Athlete` (server persists metrics updates on edit).

## Wellness

- `POST /api/wellness/submit`
  - Request: `{ athleteId, date, fatigue, stress, sleepQuality, motivation, muscleSoreness, notes?, restingHr?, hrv?, bodyWeight?, sleepHours? }`
  - Invariants: subjective scores `1..10`, numeric values non-negative.
- `GET /api/wellness/reports/athletes/{athleteId}?startDate=...&endDate=...`
  - Response: report including readiness trend and notes.

## Training Plans

- `POST /api/training-plans/generate`
  - Request: `{ athlete: Athlete, phase: string, startDate: YYYY-MM-DD, targetWeeklyHours: number }`
  - Response: `TrainingPlan` (not persisted yet).

## Reports & Exports

- Weekly report response contains:
  - `readinessTrend`, `complianceSummary`, `keyNotes`.
- CSV/JSON exports must use stable column keys and include the above sections.
