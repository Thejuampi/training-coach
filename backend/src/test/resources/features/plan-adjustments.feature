Feature: Plan adjustments workflow

  Background:
    Given a coach user exists

  Scenario: Propose and approve a plan adjustment
    Given a published plan exists for a saved athlete
    And the athlete has low readiness for 3 consecutive days
    When the coach proposes a plan adjustment to reduce intensity by 1 level
    And the system applies safety guardrails
    And the coach approves the adjustment
    Then the adjustment is applied to the plan
    And the adjustment is recorded in the plan audit log

  Scenario: Reject an unsafe adjustment
    Given a published plan exists for a saved athlete
    When the coach proposes increasing weekly load by 40 percent
    Then the adjustment is rejected by guardrails
    And the coach sees the blocking reason
