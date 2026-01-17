# Acceptance Scenario Catalog (Documentation)

This file contains **non-executable** acceptance scenarios that document intended behavior, including future capabilities not yet implemented. It is meant to complement the executable Cucumber features under `src/test/resources/features/`.

## Conventions

- These scenarios are written in a compact Given/When/Then style, but are not tied to step definitions.
- When a scenario is implemented, it should be moved into a `.feature` file and covered by tests.
- Reference feature epics from `docs/FEATURES.md` using `F#` labels.

## F2 Athlete Management

- Scenario: Coach sets baseline metrics at onboarding
  - Given a coach creates an athlete with unknown FTP/FTHR
  - When the coach schedules a baseline test protocol
  - Then the athlete profile is marked "baseline pending"
  - And zones are not used for prescriptions until baseline exists

## F4 Sync

- Scenario: Sync reports partial failure but preserves successful data
  - Given an athlete is linked to a platform
  - When a sync run ingests activities but wellness API fails
  - Then activities are persisted
  - And the sync run is recorded as "partial failure"
  - And the UI shows wellness as "stale"

## F5 Wellness & Readiness

- Scenario: Readiness confidence reflects missing data
  - Given only subjective wellness is present for the last 7 days
  - When the readiness dashboard is viewed
  - Then readiness is shown with low confidence
  - And recommendations avoid prescriptive intensity changes

- Scenario: Athlete corrects a wellness entry
  - Given an athlete submitted wellness for a date
  - When the athlete edits the entry
  - Then the snapshot is updated and versioned
  - And readiness is recomputed

## F6 Plan Lifecycle

- Scenario: Coach publishes a plan and athlete receives it
  - Given a plan draft exists for an athlete
  - When the coach publishes the plan
  - Then the athlete sees the plan on the calendar
  - And the plan has a version id and publish timestamp

- Scenario: Plan revision keeps history
  - Given a published plan exists
  - When the coach applies an adjustment
  - Then a new plan version is created
  - And the prior version remains viewable

## F8 Compliance & Progress

- Scenario: Compliance matches planned workouts with tolerance
  - Given a planned workout exists for today
  - And an activity is completed within a time tolerance window
  - When compliance is computed
  - Then the activity is matched to the planned workout
  - And compliance reflects duration and intensity deltas

## F11 Testing & Zones

- Scenario: FTP update recalculates future prescriptions only
  - Given a published plan exists
  - When FTP is updated from a new test
  - Then future workouts are recalculated to new targets
  - And completed workouts remain unchanged

## F12 Availability

- Scenario: Availability exception triggers replan
  - Given an athlete sets travel days next week
  - When the coach views the plan
  - Then conflicting sessions are flagged
  - And the coach can apply an auto-reschedule within guardrails

## F13 Events

- Scenario: Race event triggers taper
  - Given an athlete has a priority race on a date
  - When a plan is generated ending at the race date
  - Then the plan includes a taper block
  - And intensity distribution remains within safety caps

