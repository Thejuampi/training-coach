Feature: Safety and guardrails

  Safety-first training protection aligned with Seiler's "protect the intensity" philosophy
  and Foster's "train smart, not just hard" principles. This feature ensures that
  high-intensity sessions are blocked when fatigue is elevated, load progression is
  controlled, AI suggestions are constrained by safety logic, and all guardrail
  changes are auditable for compliance.

  Background:
    Given the system is running
    And readiness < 40 is considered low readiness
    And fatigue >= 8 or soreness >= 8 flags as high fatigue
    And the weekly load ramp cap defaults to 15 percent
    And minimum recovery days defaults to 2

  Scenario: Block intensity when fatigue flags are present
    Given a saved athlete has fatigue score 9 and soreness score 9 today
    When the coach attempts to schedule an intervals workout tomorrow
    Then the change is blocked by a safety rule
    And the system provides a blocking reason mentioning high fatigue or soreness
    And safe alternatives are suggested
    And the suggested alternatives include recovery options
    And the athlete is notified of recovery prioritization

  Scenario: Cap week-over-week load ramp
    Given a saved athlete has last week load 300 TSS
    When the coach proposes next week load 420 TSS
    Then the system blocks the proposal
    And the system explains the ramp cap rule with rationale
    And the rationale includes that the load increase exceeds 15 percent
    And the system suggests a maximum safe load for next week

  Scenario: Enforce minimum recovery days between high-intensity sessions
    Given the athlete completed a high-intensity interval session yesterday
    When the coach attempts to schedule another interval session today
    Then the system blocks the workout
    And the system recommends active recovery or rest instead
    And the recommended alternative is marked as recovery zone 1

  Scenario: AI suggestions must obey guardrails
    Given a saved athlete has low readiness and missed workouts
    When the coach asks AI for plan adjustment suggestions
    Then AI suggestions are filtered to safe options
    And unsafe suggestions are rejected with reasons
    And the filtering logic references the athlete's readiness score

  Scenario: Allow guardrail override with justification
    Given an admin user is logged in with override permissions
    When the admin overrides the load ramp cap for a specific athlete
    And a justification note is provided: "Athlete is in peak race preparation phase"
    Then the change is allowed
    And the override is logged with the note and timestamp
    And the audit trail records the admin user identity

  Scenario: Configure guardrail thresholds
    Given the admin opens guardrail configuration
    When the admin sets weekly ramp cap to 15 percent
    And the admin sets minimum recovery days to 2
    Then the guardrail settings are saved
    And changes are recorded in the audit log
    And the audit log includes who made the change and when
    And previous settings are versioned for potential rollback
