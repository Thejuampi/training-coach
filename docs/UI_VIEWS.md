# Training Coach UI Views (Implementation-Agnostic)

This document enumerates the UI views/screens for the Training Coach client(s). It is intended to be shared across a Java TUI and a future web UI. The UI follows **Model-View-Presenter (MVP)**: presenters are UI-framework-agnostic and depend only on ports (gateways) and navigation.

## Assumptions

- The UI runs locally and connects to the backend via REST (`server.port` default `8080`; typical base URL `http://localhost:8080`).
- Authentication uses JWT access + refresh tokens via `/api/auth/*`; UI handles login, refresh, and logout.
- Role-based behavior is derived from authenticated `SystemUser.role` claims.
- Server-push is not implemented; "async updates" are achieved via polling + invalidation + cancellable requests.
- The UI must be resilient to partial backend support (some use cases exist only as calculators or fire-and-forget endpoints).

## Roles

- **Athlete**: submits wellness, views readiness/reports, views activities and notes.
- **Coach**: manages athletes, triggers sync, generates plans, adds notes, reviews dashboards.
- **Admin**: manages users/preferences and integrations.

## Session Context (Shared State)

Presenters read/write a small set of shared session state:

- `baseUrl`: backend root URL (e.g., `http://localhost:8080`)
- `accessToken`, `refreshToken`, `sessionExpiresAt`
- `currentUserId`, `currentUserRole`
- `currentAthleteId` (may be `null` until selected)
- `refreshPolicy`: per-view polling cadence defaults
- `lastErrors`: recent failures for the notifications view

## Use Cases -> Views Mapping (Acceptance-Oriented)

These are the primary end-to-end flows reflected in `src/test/resources/features/*.feature`.

- **UC1 Manage Athlete (coach)**: `AthleteCreateView` -> `AthleteDetailView` -> `AthleteEditView`
  - Backend calls: `POST /api/athletes`, `GET /api/athletes/{id}`, `PUT /api/athletes/{id}`
- **UC0 Authenticate & Start Session**: `SplashView` -> `ConnectionSettingsView` -> `SessionView`
  - Backend calls: `POST /api/auth/login`, `POST /api/auth/refresh`, `POST /api/auth/logout`
- **UC2 Sync Athlete Data (system/coach/admin)**: `SyncTriggerView` -> (optional watcher) -> `ActivityRangeView` + `WellnessLatestView`
  - Backend calls: `POST /api/sync/athletes/{athleteId}`, `GET /api/activities/...`, `GET /api/wellness/...`
- **UC3 Generate Training Plan (coach)**: `PlanGenerateWizardView` -> `PlanPreviewView`
  - Backend calls: `GET /api/athletes/{id}` then `POST /api/training-plans/generate`
- **UC4 Review Readiness & Wellness (athlete/coach)**: `WellnessSubmitView` + `WellnessDashboardView` + `WellnessLatestView` + `WellnessHistoryView`
  - Backend calls: `POST /api/wellness/submit`, `GET /api/wellness/...`
  - Optional calculator: `ReadinessCalculatorView` via `POST /api/analysis/readiness`
- **UC5 Track Compliance/Progress (athlete/coach)**: `ComplianceProgressView` (limited today; see backend gaps)
  - Backend calls today: `POST /api/analysis/compliance` (calculator only)
- **UC6 Adjust Plan (coach)**: `AdjustmentSuggestionView` (calculator / assistant)
  - Backend calls: `POST /api/analysis/adjustments` (and optionally AI suggestions)
- **UC7 Coach Communication (coach/athlete)**: `NotesListView` + `NoteAddView`
  - Backend calls: `POST /api/notes/athletes/{athleteId}`, `GET /api/notes/athletes/{athleteId}`
- **UC8 Configure Integrations (admin)**: `IntervalsIcuConfigView` + `IntegrationsStatusView`
  - Backend calls: `POST /api/integrations/intervals-icu`, `GET /api/integrations/intervals-icu`
- **UC9 Review Activity History (athlete/coach)**: `ActivityRangeView` + `ActivityDayView`
  - Backend calls: `GET /api/activities/...`

## Navigation Map (Route Keys)

Route keys are UI-framework-agnostic identifiers used by presenters.

- `app:splash` -> `SplashView`
- `app:connection-settings` -> `ConnectionSettingsView`
- `app:session` -> `SessionView`
- `app:menu` -> `MainMenuView`
- `users:list` / `users:new` / `users:detail:{userId}` / `users:prefs:{userId}`
- `users:privacy:{userId}`
- `athletes:list` / `athletes:new` / `athletes:detail:{athleteId}` / `athletes:edit:{athleteId}`
- `sync:trigger:{athleteId}` / `sync:status:{athleteId}`
- `wellness:dashboard:{athleteId}` / `wellness:latest:{athleteId}` / `wellness:history:{athleteId}` / `wellness:detail:{athleteId}:{date}` / `wellness:submit:{athleteId}`
- `activities:range:{athleteId}` / `activities:day:{athleteId}:{date}`
- `plans:generate:{athleteId}` / `plans:preview:{athleteId}` / `plans:list:{athleteId}` / `plans:detail:{athleteId}:{planId}`
- `compliance:progress:{athleteId}`
- `notes:list:{athleteId}` / `notes:add:{athleteId}`
- `integrations:status` / `integrations:intervals-icu` / `integrations:ai-suggestion`
- `advanced:readiness` / `advanced:compliance` / `advanced:adjustments` / `advanced:trends`
- `reports:weekly:{athleteId}` / `reports:export:{athleteId}` / `reports:org`
- `help` / `about`

## Role-to-View Access (Initial)

This is a product-level policy; enforcement lives in presenters and navigation.

| Area | Athlete | Coach | Admin |
|------|---------|-------|-------|
| Session + menu | Yes | Yes | Yes |
| Users | No | Read-only | Yes |
| Athletes | Context-only | Yes | Yes |
| Sync trigger/status | No | Yes | Yes |
| Wellness | Yes | Yes | Yes |
| Activities | Yes | Yes | Yes |
| Plans generate | No | Yes | Yes |
| Notes add | No | Yes | Yes |
| Integrations | No | Limited | Yes |
| Analysis tools | No | Yes | Yes |

## Menu Tree (Per Role)

This is the first-pass navigation structure. Presenters may hide items when required context (e.g., no athlete selected) is missing.

### Athlete Menu

- Athlete
  - Switch athlete (`athletes:list`)
  - Overview (`wellness:dashboard:{athleteId}`)
  - Wellness
    - Submit (`wellness:submit:{athleteId}`)
    - Latest (`wellness:latest:{athleteId}`)
    - History (`wellness:history:{athleteId}`)
    - Recommendations (`wellness:recommendations:{athleteId}`)
  - Activities
    - Range (`activities:range:{athleteId}`)
    - By day (`activities:day:{athleteId}:{date}`)
  - Notes (`notes:list:{athleteId}`)
  - Settings
    - Preferences (`users:prefs:{userId}`)
    - Privacy & data (`users:privacy:{userId}`)
  - Help/About (`help`, `about`)

### Coach Menu

- Coach
  - Athletes
    - List (`athletes:list`)
    - New (`athletes:new`)
    - Details/Edit (`athletes:detail:{athleteId}`, `athletes:edit:{athleteId}`)
  - Athlete workspace (requires selected athlete)
    - Overview (`wellness:dashboard:{athleteId}`)
    - Sync (`sync:trigger:{athleteId}`, `sync:status:{athleteId}`)
    - Wellness (same as athlete menu)
    - Activities (same as athlete menu)
    - Plans
      - Generate (`plans:generate:{athleteId}`)
      - Preview (`plans:preview:{athleteId}`)
      - Saved plans (`plans:list:{athleteId}`) (backend gap)
    - Compliance/Progress (`compliance:progress:{athleteId}`) (limited today)
    - Notes (`notes:list:{athleteId}`, `notes:add:{athleteId}`)
  - Tools (Advanced)
    - Readiness (`advanced:readiness`)
    - Compliance (`advanced:compliance`)
    - Adjustments (`advanced:adjustments`)
    - Trends (`advanced:trends`)
  - Reports
    - Weekly report (`reports:weekly:{athleteId}`)
    - Export report (`reports:export:{athleteId}`)
  - AI prompt (`integrations:ai-suggestion`)
  - Settings
    - Preferences (`users:prefs:{userId}`)
    - Privacy & data (`users:privacy:{userId}`)
  - Help/About (`help`, `about`)

### Admin Menu

- Admin
  - Users (`users:list`, `users:new`, `users:detail:{userId}`, `users:prefs:{userId}`)
  - Athletes (same as coach)
  - Integrations (`integrations:status`, `integrations:intervals-icu`)
  - Tools (Advanced) (same as coach)
  - Reports
    - Organization summary (`reports:org`)
    - Weekly report (`reports:weekly:{athleteId}`)
    - Export report (`reports:export:{athleteId}`)
  - Privacy requests (`users:privacy`)
  - Help/About (`help`, `about`)

## View Catalog

### Boot / Shell

- `SplashView` (all): startup status, backend connectivity.
- `ConnectionSettingsView` (all): set `baseUrl`, timeouts, refresh rates.
- `SessionView` (all): login, select current athlete context, manage session refresh.
- `MainMenuView` (all): role-aware navigation.

### Users (Admin)

- `UserListView` (admin/coach): list/search users. (`GET /api/users`)
- `UserCreateView` (admin): create user. (`POST /api/users`)
- `UserDetailView` (admin/coach/self): show user details. (`GET /api/users/{id}`)
- `UserPreferencesView` (admin/self): view/edit preferences. (`GET /api/users/{id}/preferences`, `PUT /api/users/{id}/preferences`)
- `CredentialStatusView` (admin): view credential status without exposing secrets. (`GET /api/users/{id}/credentials`)
- `PrivacyAndDataView` (admin/self): export/delete data requests. (backend gap; see UC20)
  - Backend calls (future): `POST /api/privacy/exports`, `POST /api/privacy/deletions`, `GET /api/privacy/requests`

### Athletes

- `AthleteListView` (coach/admin): list/search athletes. (`GET /api/athletes`)
- `AthleteCreateView` (coach/admin): create athlete. (`POST /api/athletes`)
- `AthleteDetailView` (all): athlete profile/metrics/preferences summary. (`GET /api/athletes/{id}`)
- `AthleteEditView` (coach/admin): edit athlete. (`PUT /api/athletes/{id}`)
- `AthleteDeleteConfirmView` (coach/admin): delete athlete. (`DELETE /api/athletes/{id}`)
- `AthleteSwitchView` (all): change current athlete context. (`GET /api/athletes`)

### Sync

- `SyncTriggerView` (coach/admin): trigger backfill sync. (`POST /api/sync/athletes/{athleteId}?daysBack=...`)
- `SyncStatusView` (coach/admin): progress/history/errors. (backend gap; see "Missing Backend Endpoints")

### Wellness

- `WellnessDashboardView` (all): readiness summary and flags. (`GET /api/wellness/dashboard/athletes/{athleteId}?days=...`)
- `WellnessLatestView` (all): latest snapshot drill-in. (`GET /api/wellness/athletes/{athleteId}/latest`)
- `WellnessHistoryView` (all): history table by date range. (`GET /api/wellness/athletes/{athleteId}/history?startDate=...&endDate=...`)
- `WellnessDetailView` (all): snapshot by date. (`GET /api/wellness/athletes/{athleteId}/date/{date}`)
- `WellnessSubmitView` (athlete/coach): submit wellness entry. (`POST /api/wellness/submit`)
- `WellnessDeleteConfirmView` (athlete/coach/admin): delete wellness entry. (`DELETE /api/wellness/athletes/{athleteId}/date/{date}`)
- `WellnessReportView` (coach/admin): report across range. (`GET /api/wellness/reports/athletes/{athleteId}?startDate=...&endDate=...`)
- `RecoveryRecommendationsView` (all): recommendations view. (`GET /api/wellness/recommendations/athletes/{athleteId}?days=...`)

### Activities

- `ActivityRangeView` (all): list activities for range. (`GET /api/activities/athletes/{athleteId}?startDate=...&endDate=...`)
- `ActivityDayView` (all): activity by date. (`GET /api/activities/athletes/{athleteId}/date/{date}`)

### Training Plans

- `PlanGenerateWizardView` (coach): generate plan. (`POST /api/training-plans/generate`)
- `PlanPreviewView` (coach/athlete): display generated plan (client-cached for now).
- `PlanListView` / `PlanDetailView` (coach/athlete): list/view saved plans. (backend gap; see "Missing Backend Endpoints")

### Compliance / Progress

- `ComplianceProgressView` (coach/athlete): compliance and progress view. (limited today; see "View Specifications" and "Missing Backend Endpoints")

### Notes / Communication

- `NotesListView` (all): list notes for athlete. (`GET /api/notes/athletes/{athleteId}`)
- `NoteAddView` (coach): add a note. (`POST /api/notes/athletes/{athleteId}`)

### Analysis Tools (Optional)

These are utility screens and can be hidden behind an "Advanced" menu.

- `ReadinessCalculatorView` (coach/admin): ad-hoc readiness. (`POST /api/analysis/readiness`)
- `ComplianceCalculatorView` (coach/admin): ad-hoc compliance. (`POST /api/analysis/compliance`)
- `AdjustmentSuggestionView` (coach/admin): ad-hoc adjustment text. (`POST /api/analysis/adjustments`)
- `TrendToolView` (coach/admin): ad-hoc trend. (`POST /api/analysis/trends`)

### Integrations

- `IntegrationsStatusView` (admin/coach): show integration status. (`GET /api/integrations/intervals-icu`)
- `IntervalsIcuConfigView` (admin): configure API key. (`POST /api/integrations/intervals-icu`)
- `AiSuggestionView` (coach/admin): prompt + result. (`POST /api/integrations/ai/suggestions`)

### Reports & Exports

- `WeeklyReportView` (coach/admin): weekly report for athlete. (`GET /api/reports/weekly?...`) (backend gap)
- `OrgReportView` (admin): organization summary report. (backend gap)
- `ReportExportView` (coach/admin): export CSV/JSON for a report. (`POST /api/reports/exports`) (backend gap)

### Cross-Cutting

- `NotificationsView` (all): background refresh results, sync completion, errors.
- `ErrorDetailsView` (all): request/response diagnostics (redact secrets).
- `HelpView` / `KeybindingsView` / `AboutView` (all).

## View Specifications (Minimum Required Details)

This section defines the behavior each presenter must implement (inputs, actions, API calls, and states). If a view is not mentioned here, it is not considered "ready to implement".

### Common UI Patterns (All Views)

- **Loading state**: show spinner/status line while requests are in flight.
- **Empty state**: explicitly distinguish "no data" from "failed to load".
- **Error state**: show a concise message + a "details" affordance (HTTP status, method, path, truncated body), but redact secrets.
- **Manual refresh**: every data view supports an explicit refresh action.
- **Cancellation**: navigating away cancels in-flight requests and stops polling.
- **Date input**: `YYYY-MM-DD` only; validate and normalize before calling API.

### `SessionView`

- Requires: `baseUrl`
- Loads:
  - `POST /api/auth/login` (authenticate, receive tokens)
  - `GET /api/athletes` (populate athlete selection; can be delayed until role/user chosen)
- Actions:
  - Login -> sets `accessToken`, `refreshToken`, `currentUserRole`
  - Select athlete -> sets `currentAthleteId`
  - Continue -> `Navigator.goTo(app:menu)`
- Empty/error:
  - Backend unreachable -> guide to `ConnectionSettingsView`
  - Auth failed -> show error and retry options

### `AthleteListView`

- Loads: `GET /api/athletes`
- Actions:
  - Select athlete -> sets `currentAthleteId`, navigate to `AthleteDetailView`
  - New -> `AthleteCreateView`
  - Refresh

### `AthleteCreateView` / `AthleteEditView`

- Loads:
  - Create: none
  - Edit: `GET /api/athletes/{id}`
- Submits:
  - Create: `POST /api/athletes`
  - Edit: `PUT /api/athletes/{id}`
- Inputs (UI model):
  - `name`
  - `profile`: gender, age, weightKg, heightCm, level
  - `preferences`: available days, target weekly volume hours, current phase
  - `metrics`: ftp, fthr, vo2max, weightKg (optional; see backend note)
- Validation:
  - Name non-empty
  - Age >= 0
  - Weekly volume hours >= 0
  - Available days parseable (comma-separated DayOfWeek tokens)
- Important backend note (current behavior):
  - `POST /api/athletes` ignores submitted metrics; it uses default metrics in `AthleteService.createAthlete(...)`.
  - `PUT /api/athletes/{id}` can update metrics (it persists `updatedAthlete.currentMetrics()`).

### `SyncTriggerView` (and watcher behavior)

- Submits: `POST /api/sync/athletes/{athleteId}?daysBack=...`
- Actions:
  - Trigger sync (fire-and-forget)
  - Optional "watch results": poll `GET /api/wellness/athletes/{athleteId}/latest` and a recent `GET /api/activities/...` range until change or timeout
- States:
  - There is no sync run status endpoint; UI must communicate that progress is inferred.

### `WellnessSubmitView`

- Submits: `POST /api/wellness/submit`
- Inputs (from `WellnessController.WellnessSubmissionRequest`):
  - `athleteId`, `date`
  - subjective: fatigue/stress/sleepQuality/motivation/muscleSoreness (1..10) + notes
  - physiological: resting HR, HRV, body weight (kg), sleep hours
- Validation:
  - Subjective scores in `1..10`
  - Numeric units are non-negative
- After success:
  - Navigate to `wellness:latest:{athleteId}` and show a success notification

### `WellnessDashboardView` / `WellnessLatestView` / `WellnessHistoryView` / `WellnessDetailView`

- Loads:
  - Dashboard: `GET /api/wellness/dashboard/athletes/{athleteId}?days=...`
  - Latest: `GET /api/wellness/athletes/{athleteId}/latest`
  - History: `GET /api/wellness/athletes/{athleteId}/history?startDate=...&endDate=...`
  - Detail: `GET /api/wellness/athletes/{athleteId}/date/{date}`
- Refresh:
  - Dashboard + latest: poll every 5-10s while visible (configurable)
  - History/detail: manual refresh by default; optional slow polling

### `ActivityRangeView` / `ActivityDayView`

- Loads:
  - Range: `GET /api/activities/athletes/{athleteId}?startDate=...&endDate=...`
  - Day: `GET /api/activities/athletes/{athleteId}/date/{date}`
- Validation:
  - Range max 365 days (backend enforces); UI should enforce and show a friendly error before calling.
- Refresh:
  - Manual refresh by default; optional slow polling

### `NotesListView` / `NoteAddView`

- Loads: `GET /api/notes/athletes/{athleteId}`
- Submits: `POST /api/notes/athletes/{athleteId}`
- Validation:
  - Note non-blank
- Important backend note (current behavior):
  - Notes are stored in-memory (`NoteService` uses a `Map`); they reset on application restart.

### `PlanGenerateWizardView` / `PlanPreviewView`

- Loads: `GET /api/athletes/{athleteId}` (to embed the `Athlete` object in the request)
- Submits: `POST /api/training-plans/generate`
- Inputs:
  - `phase`, `startDate`, `targetWeeklyHours`
- Validation:
  - Weekly hours >= 0
  - `startDate` valid date
- Important backend note (current behavior):
  - There are no endpoints to list/get persisted plans; preview must be stored client-side until plan persistence exists.

### `ComplianceProgressView` (UC5)

- Goal: track planned vs completed compliance and progress trends.
- Backend support today:
  - Only calculator exists: `POST /api/analysis/compliance` accepts counts, not actual plan/activity matching.
- UI behavior today:
  - Provide a minimal calculator UI and label it as "manual input" until plan persistence + matching exists.

### `IntervalsIcuConfigView` / `IntegrationsStatusView` (UC8)

- Submits: `POST /api/integrations/intervals-icu`
- Loads: `GET /api/integrations/intervals-icu`
- Important backend note (current behavior):
  - The stored key is not used by the sync adapter configuration (`intervals.icu.api-key` is read from env/config).
  - Do not print the raw API key in UI logs; treat it as a secret even if backend returns it.

### `AiSuggestionView`

- Submits: `POST /api/integrations/ai/suggestions`
- Inputs: `prompt` (string)
- Behavior:
  - Show request/response with truncation; allow copy; keep a local history.

## Presenter Responsibilities (Per View)

For each `XxxView`, create an `XxxPresenter` that:

- Loads initial data and maps it to an immutable `XxxViewModel` (Java records work well).
- Validates user inputs before calling the gateway (UX validation; server remains source of truth).
- Executes gateway calls asynchronously and updates the view model + notifications.
- Controls navigation (`Navigator.goTo(routeKey)`).
- Applies role rules (disable/hide actions; show explanatory messages).

## Key Request Shapes (Backend Contracts)

These endpoints typically need UI-side DTOs and careful validation.

### Training Plan Generation

Endpoint: `POST /api/training-plans/generate`

Current contract (`GeneratePlanRequest`) requires a full `Athlete` object in the request body, plus:

- `phase` (`String`)
- `startDate` (`YYYY-MM-DD`)
- `targetWeeklyHours` (`Hours` JSON number)

UI guidance:

- Fetch the athlete first via `GET /api/athletes/{id}` and send that as `athlete` in the request.
- Treat `targetWeeklyHours` as a number (e.g., `6.5`).

### Wellness Submission

Endpoint: `POST /api/wellness/submit`

UI guidance:

- Scores are `1..10` for subjective fields.
- Units are JSON numbers for value objects (e.g., `BeatsPerMinute`, `HeartRateVariability`, `Hours`).
- Some fields can be omitted (`null`) depending on what the user has.

## Background Refresh (Polling Strategy)

Until server-push exists, the UI uses polling with cancellation.

- When a view is visible, its presenter may start a periodic refresh job.
- When the user navigates away, the presenter cancels outstanding requests and stops the job.
- Suggested defaults:
  - Dashboard/latest/recommendations: 5-10s while visible.
  - Lists/history/ranges: 30-60s while visible (or manual refresh).
  - After `POST /api/sync/...`: run a short "watcher" that polls relevant read endpoints for changes and then notifies.

## MVP Architecture Notes (MVP + "UI is an implementation detail")

- Define a UI-agnostic port such as `TrainingCoachGateway` with methods matching backend capabilities (e.g., `listAthletes`, `submitWellness`, `triggerSync`, ...).
- Implement `RestTrainingCoachGateway` for the Java TUI now.
- For a future web UI, keep presenters reusable by swapping only the view implementation and/or gateway.

## Missing Backend Endpoints (Recommended)

These gaps block some views from being first-class without workarounds.

- **Sync status/progress**: a `SyncRun` resource or SSE/WebSocket updates for `SyncStatusView`.
- **Training plan persistence/retrieval**: endpoints for saving and listing plans (`PlanListView`, `PlanDetailView`).
- **Auth/session**: real identity + permissions; remove the "select a user" workaround.
- **Real compliance/progress**: matching planned workouts to completed activities (requires plan persistence + linkage).
- **Secret handling**: do not return stored API keys from `GET /api/integrations/intervals-icu` (return status/metadata instead).
