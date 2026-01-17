# Training Coach Feature Documentation

This document is the canonical, implementation-agnostic description of product features. It complements:

- `docs/USE_CASES.md` (why/what at a use-case level)
- `docs/UI_VIEWS.md` (how users navigate and what screens exist)
- Cucumber features under `src/test/resources/features/` (current acceptance coverage)

## Conventions

- Status:
  - **Now**: must exist for a usable MVP.
  - **Soon**: required for a credible training app, next iterations.
  - **Later**: planned, but not required to validate the product.
- Each feature includes:
  - Purpose
  - Actors & permissions
  - Core workflow
  - Data inputs/outputs
  - Edge cases and non-goals
  - Current backend support (endpoints) vs gaps

## Feature Catalog (Epics)

### Seiler 3-Zone Model (Cross-Cutting Principle)

This app models intensity distribution using LT1/LT2 and the **Seiler 3-zone** model:

- Z1: below LT1
- Z2: between LT1 and LT2
- Z3: above LT2

See `docs/SEILER_INTENSITY_MODEL.md`.

This includes both:

- **Distribution**: Z1/Z2/Z3 time relative to LT1/LT2
- **Prescription target bands**: FATMAX within Z1, and VO2-optimal vs sprint/neuromuscular within Z3
- **Method + confidence**: prescription bands carry method and confidence for transparency

### F1. Identity, Roles, and Permissions (Status: Now)

**Purpose:** Distinguish Athlete/Coach/Admin capabilities and prevent accidental misuse.

- Actors: Athlete, Coach, Admin
- Core workflow:
  - Admin creates users and assigns roles.
  - Users have preferences (units) that affect presentation and input defaults.
- Data:
  - User: id, name, role
  - Preferences: measurement system + unit preferences
- Current backend support:
  - `POST /api/users`, `GET /api/users`, `GET /api/users/{id}`
  - `GET /api/users/{id}/preferences`, `PUT /api/users/{id}/preferences`
  - `GET /api/users/{id}/credentials` (status-only)
- Gaps / notes:
  - Authorization is enforced via JWT roles; ensure role claims remain current.
  - Credential status must not expose hashes or secrets.

### F0. Authentication & Session Management (Status: Now)

**Purpose:** Provide secure login, session refresh, and logout across UI clients.

- Actors: Athlete, Coach, Admin
- Core workflow:
  - User logs in with username/password.
  - System issues access + refresh tokens.
  - Client refreshes session before expiry; logout revokes refresh tokens.
- Data:
  - Access token (JWT), refresh token, token expiry, role claims
- Current backend support:
  - `POST /api/auth/login`, `POST /api/auth/refresh`, `POST /api/auth/logout`
  - `/.well-known/jwks.json` for verification
- Gaps / notes:
  - Add token revocation visibility in admin tooling (future).

### F2. Athlete Management (Status: Now)

**Purpose:** Represent the athlete, their preferences, and training baseline.

- Actors: Coach (primary), Admin (secondary), Athlete (consent/data subject)
- Core workflow:
  - Create athlete with profile (age, level) and preferences (availability, weekly hours, phase).
  - Maintain baseline metrics (FTP/FTHR/VO2/weight).
- Data:
  - Athlete profile: gender, age, height, weight, level
  - Preferences: available days, target weekly volume hours, phase
  - Metrics: ftp, fthr, vo2max, weight
- Current backend support:
  - `POST /api/athletes`, `GET /api/athletes`, `GET /api/athletes/{id}`, `PUT /api/athletes/{id}`, `DELETE /api/athletes/{id}`
- Important constraints / caveats:
  - Current create flow sets default metrics server-side (submitted metrics are not used); updating metrics requires `PUT`.

### F3. Integrations & Data Sources (Status: Now)

**Purpose:** Connect external platforms (Intervals.icu first) for activities and wellness inputs.

- Actors: Admin (primary), Coach (limited)
- Core workflow:
  - Provide credentials, validate connectivity, set sync policy (cadence, scope).
  - Monitor integration health (last success, errors).
- Data:
  - Credential secret(s), scopes, status, last sync time, error history
- Current backend support:
  - `POST /api/integrations/intervals-icu`, `GET /api/integrations/intervals-icu`
- Gaps / notes:
  - Current stored API key is not wired to the sync adapter configuration (adapter reads `INTERVALS_ICU_API_KEY`).
  - `GET /api/integrations/intervals-icu` returns the key; this should be replaced by status-only output.

### F4. Data Sync (Activities + Wellness) (Status: Now)

**Purpose:** Ingest activities and wellness data from a platform into local storage.

- Actors: System (scheduler), Coach/Admin (manual trigger)
- Core workflow:
  - Trigger sync (scheduled or manual backfill).
  - Fetch remote activities and wellness data by date range/checkpoint.
  - Persist locally; run derived calculations (training load summary, readiness).
- Data:
  - Activities: date, duration, distance, TSS/IF, power/HR summaries
  - Wellness: RHR/HRV/sleep/weight + subjective wellness
- Current backend support:
  - Trigger: `POST /api/sync/athletes/{athleteId}?daysBack=...`
  - Read activities: `GET /api/activities/athletes/{athleteId}?...`, `GET /api/activities/athletes/{athleteId}/date/{date}`
  - Read wellness: `GET /api/wellness/...`
- Gaps / notes:
  - No sync run status/progress/history endpoint; UI must treat sync as fire-and-forget.
  - No explicit sync event log API yet (even though the architecture doc anticipates it).

### F5. Wellness Logging & Readiness (Status: Now)

**Purpose:** Capture subjective wellness and compute readiness combining signals and load.

- Actors: Athlete (primary input), Coach (review)
- Core workflow:
  - Athlete submits daily wellness (scores + optional physiological data).
  - System stores snapshot and computes readiness score.
  - Dashboards and reports show trends, flags, and recommendations.
- Data:
  - Subjective: fatigue, stress, sleep quality, motivation, soreness, notes
  - Physiological: resting HR, HRV, body weight, sleep hours/quality
  - Derived: readiness score (0-100), load summary, flags
- Current backend support:
  - Submit: `POST /api/wellness/submit`
  - Query: `GET /api/wellness/athletes/{athleteId}/latest`, `/history`, `/date/{date}`
  - Dashboard: `GET /api/wellness/dashboard/athletes/{athleteId}?days=...`
  - Report: `GET /api/wellness/reports/athletes/{athleteId}?startDate=...&endDate=...`
  - Recommendations: `GET /api/wellness/recommendations/athletes/{athleteId}?days=...`
- Gaps / notes:
  - Confidence/coverage should be explicit in dashboards when data is missing.

### F6. Training Plan Lifecycle (Status: Now -> Soon)

**Purpose:** Create, review, publish, revise, and archive training plans.

- Actors: Coach (primary), Athlete (consumer)
- Core workflow:
  - Generate a plan draft from athlete preferences/phase/start date/target weekly hours.
  - Review weekly structure (key sessions + easy volume).
  - Publish to athlete; preserve plan versions on changes.
- Data:
  - Plan: phase, start/end dates, total volume, notes
  - Workouts: date, type, duration, intervals/targets, intensity distribution
- Current backend support:
  - Generate only: `POST /api/training-plans/generate` (requires embedding full `Athlete` object in request)
- Gaps (required for "real" lifecycle):
  - Persist plan, list plans, fetch plan, version plans, publish status, archive.

### F7. Workout Execution & Feedback (Status: Soon)

**Purpose:** Ensure the athlete can execute the plan and provide feedback beyond activity sync.

- Actors: Athlete (primary), Coach (review)
- Core workflow:
  - Athlete views today's workout targets and completes it.
  - System matches completed activity to planned workout (tolerances).
  - Athlete optionally logs RPE, pain/injury flags, and subjective difficulty.
- Gaps:
  - Plan persistence + linkage from activity -> planned workout
  - Structured feedback endpoints (RPE, pain flags) and storage

### F8. Compliance & Progress (Status: Soon)

**Purpose:** Measure adherence and long-term progress (training load trend, streaks, distribution).

- Actors: Coach (primary), Athlete (secondary)
- Core workflow:
  - Match planned workouts to completed activities.
  - Compute compliance metrics and progress trend summaries (including Z1/Z2/Z3 time distribution and "Z2 creep").
  - Provide weekly reviews and alerts.
- Current backend support:
  - Calculator only: `POST /api/analysis/compliance` (planned vs completed counts)
- Gaps:
  - Real matching, real compliance model, weekly report endpoints, persistence.

### F9. Plan Adjustments (Status: Soon)

**Purpose:** Safely adapt training based on readiness, compliance, and constraints.

- Actors: Coach (primary), System (guardrails), AI (advisory)
- Core workflow:
  - Detect triggers (low readiness, missed key sessions, travel, event changes).
  - Propose adjustments; enforce caps/guardrails; coach approves.
  - Publish updated plan; maintain audit history.
- Current backend support:
  - Calculator text only: `POST /api/analysis/adjustments`
  - AI suggestion text: `POST /api/integrations/ai/suggestions`
- Gaps:
  - Plan persistence/versioning and safe application of changes.

### F10. Coach Communication (Status: Now)

**Purpose:** Coach can provide context and guidance linked to the athlete.

- Actors: Coach (author), Athlete (reader)
- Core workflow:
  - Coach posts notes linked to an athlete (and later to plan/workout/date).
  - Athlete reads notes in context.
- Current backend support:
  - `POST /api/notes/athletes/{athleteId}`, `GET /api/notes/athletes/{athleteId}`
- Important constraints / caveats:
  - Notes are currently in-memory only; restart loses data.

### F11. Testing, Zones, and Baselines (Status: Soon)

**Purpose:** Keep zones current (FTP/FTHR) and use them for prescriptions and analysis.

- Actors: Coach/Athlete
- Core workflow:
  - Schedule tests; ingest results; update zones; apply to plan generator and analysis.
- Gaps:
  - Data model for zones and test results; endpoints and UI.

### F12. Calendar, Availability, and Exceptions (Status: Soon)

**Purpose:** Respect real-world constraints (work, travel, rest weeks).

- Actors: Athlete (primary), Coach (secondary)
- Core workflow:
  - Maintain availability template and exceptions; plan generator and adjustments must respect it.
- Gaps:
  - Availability exception model and endpoints.

### F13. Events / Races / Goals (Status: Soon)

**Purpose:** Plan around goal events with phases and taper.

- Actors: Athlete/Coach
- Core workflow:
  - Define events with dates and priority; generator uses event timeline.
- Gaps:
  - Events model + endpoints; plan generator support.

### F14. Notifications (Status: Soon)

**Purpose:** Reminders and risk alerts without overwhelming users.

- Actors: System
- Core workflow:
  - Workout reminders, wellness reminders, missed key session alerts, fatigue warnings.
- Gaps:
  - Notification delivery + preferences + scheduling.

### F15. Reports & Exports (Status: Soon)

**Purpose:** Weekly summaries and exports for coach operations.

- Actors: Coach/Admin
- Core workflow:
  - Weekly athlete report (readiness trend, compliance, notes, load).
  - Organization-level summary for admin (coverage, compliance, risk flags).
  - Export to CSV/JSON; shareable snapshots.
- Partial backend support:
  - Wellness report exists; compliance and plan reports do not.

### F16. Safety & Guardrails (Status: Now -> Soon)

**Purpose:** Prevent unsafe prescriptions and constrain AI assistance.

- Actors: System (enforce), Coach (approve)
- Core workflow:
  - Enforce load ramp caps, minimum rest, intensity blocks when fatigue flags are present.
  - Constrain AI prompts and apply deterministic checks to outputs.
- Gaps:
  - Guardrail configuration UI and audit logs.

### F20. Privacy & Data Retention (Status: Soon)

**Purpose:** Respect athlete privacy, consent, and data lifecycle requirements.

- Actors: Admin (primary), Athlete (requester)
- Core workflow:
  - Athlete requests export or deletion of personal data.
  - Admin reviews, approves, and the system executes the request.
  - All actions are recorded in an audit/consent log.
- Gaps:
  - Export and deletion endpoints are not implemented.
  - UI needs a dedicated privacy/request flow.

## Documentation Gaps (What we should add next)

- End-to-end flows per role with screenshots/wireframes (optional) and API mapping.

## Next Documentation Deliverables (Suggested)

- `docs/GLOSSARY.md`: shared definitions for metrics, zones, phases, and auth terms.
- `docs/DATA_CONTRACTS.md`: data contracts and invariants per feature.
- `docs/DEFINITION_OF_DONE.md`: definition of done checklists per feature.
- `docs/FEATURES/` folder: one file per feature epic (`F*.md`) using `docs/FEATURE_TEMPLATE.md`.
- `docs/ACCEPTANCE_SCENARIOS.md`: non-executable scenario catalog (future work can be migrated into Cucumber).
