Feature: Safety and guardrails

  Background:
    Given the system is running

  Scenario: Block intensity when fatigue flags are present
    Given a saved athlete has fatigue score 9 and soreness score 9 today
    When the coach attempts to schedule an intervals workout tomorrow
    Then the change is blocked by a safety rule
    And safe alternatives are suggested

  Scenario: Cap week-over-week load ramp
    Given a saved athlete has last week load 300 TSS
    When the coach proposes next week load 420 TSS
    Then the system blocks the proposal
    And the system explains the ramp cap rule

  Scenario: AI suggestions must obey guardrails
    Given a saved athlete has low readiness and missed workouts
    When the coach asks AI for plan adjustment suggestions
    Then AI suggestions are filtered to safe options
    And unsafe suggestions are rejected with reasons

  Scenario: Configure guardrail thresholds
    Given the admin opens guardrail configuration
    When the admin sets weekly ramp cap to 15 percent
    And the admin sets minimum recovery days to 2
    Then the guardrail settings are saved
    And changes are recorded in the audit log
