Feature: Safety and guardrails

  Safety-first training protection aligned with Seiler's "protect the intensity" philosophy
  and Foster's "train smart, not just hard" principles. This feature ensures that
  high-intensity sessions are blocked when fatigue is elevated, load progression is
  controlled, AI suggestions are constrained by safety logic, and all guardrail
  changes are auditable for compliance.

  Each guardrail rule has a unique ID for traceability, compliance auditing, and
  documentation linking. Rule IDs follow the pattern: SG-[CATEGORY]-[NUMBER]

  Background:
    Given the system is running
    And readiness < 40 is considered low readiness
    And fatigue >= 8 or soreness >= 8 flags as high fatigue
    And the weekly load ramp cap defaults to 15 percent
    And minimum recovery days defaults to 2

  Rule: SG-FATIGUE-001 - Block high-intensity when fatigue is elevated
    High fatigue (>=8) combined with high soreness (>=8) indicates the athlete
    needs recovery, not intensity. Blocks INTERVALS, VO2_MAX, THRESHOLD, SPRINT
    session types and suggests recovery alternatives.

    Scenario: Block intensity when fatigue flags are present
      Given a saved athlete has fatigue score 9 and soreness score 9 today
      When the coach attempts to schedule an intervals workout tomorrow
      Then the change is blocked by a safety rule
      And the rule ID "SG-FATIGUE-001" is triggered
      And the system provides a blocking reason mentioning high fatigue or soreness
      And safe alternatives are suggested
      And the suggested alternatives include recovery options
      And the athlete is notified of recovery prioritization

  Rule: SG-LOAD-001 - Cap weekly load progression
    Prevents excessive week-over-week load increases (>15%) which can lead to
    non-functional overreaching. Includes rationale explaining the 15% cap.

    Scenario: Cap week-over-week load ramp
      Given a saved athlete has last week load 300 TSS
      When the coach proposes next week load 420 TSS
      Then the system blocks the proposal
      And the rule ID "SG-LOAD-001" is triggered
      And the system explains the ramp cap rule with rationale
      And the rationale includes that the load increase exceeds 15 percent
      And the system suggests a maximum safe load for next week

  Rule: SG-RECOVERY-001 - Enforce minimum recovery between hard sessions
    Requires minimum 2 recovery days between high-intensity sessions to allow
    physiological adaptation. Protects against accumulated fatigue.

    Scenario: Enforce minimum recovery days between high-intensity sessions
      Given the athlete completed a high-intensity interval session yesterday
      When the coach attempts to schedule another interval session today
      Then the system blocks the workout
      And the rule ID "SG-RECOVERY-001" is triggered
      And the system recommends active recovery or rest instead
      And the recommended alternative is marked as recovery zone 1

  Rule: SG-AI-001 - Filter AI suggestions by safety guardrails
    AI-generated plan adjustments must respect readiness-based guardrails.
    Unsafe suggestions (high intensity, extra VO2 max, extra sprints) are
    filtered when readiness < 40.

    Scenario: AI suggestions must obey guardrails
      Given a saved athlete has low readiness and missed workouts
      When the coach asks AI for plan adjustment suggestions
      Then AI suggestions are filtered to safe options
      And unsafe suggestions are rejected with reasons
      And the filtering logic references the athlete's readiness score
      And the rule ID "SG-AI-001" is applied to the filtering

  Rule: SG-OVERRIDE-001 - Allow admin override with justification
    Authorized admins can override guardrails with documented justification.
    All overrides are logged with user identity, timestamp, and rationale
    for compliance and audit purposes.

    Scenario: Allow guardrail override with justification
      Given an admin user is logged in with override permissions
      When the admin overrides the load ramp cap for a specific athlete
      And a justification note is provided: "Athlete is in peak race preparation phase"
      Then the change is allowed
      And the rule ID "SG-OVERRIDE-001" is logged
      And the override is logged with the note and timestamp
      And the audit trail records the admin user identity

  Rule: SG-CONFIG-001 - Guardrail configuration management
    Admin-configurable thresholds for ramp cap and recovery days. All
    configuration changes are versioned and auditable for compliance.

    Scenario: Configure guardrail thresholds
      Given the admin opens guardrail configuration
      When the admin sets weekly ramp cap to 15 percent
      And the admin sets minimum recovery days to 2
      Then the guardrail settings are saved
      And the rule ID "SG-CONFIG-001" is logged
      And changes are recorded in the audit log
      And the audit log includes who made the change and when
      And previous settings are versioned for potential rollback
