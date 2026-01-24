Feature: Events, races, and goals

  Background:
    Given a coach user exists

    Scenario: Athlete adds a goal event
    Given a saved athlete
    When the athlete adds a goal event "Spring Classic" on "2026-03-01" priority "A"
    Then the event appears in the athlete calendar

    Scenario: Plan generator includes a taper before an A-priority race
    Given a saved athlete with a goal event on "2026-03-01" priority "A"
    When the coach generates a plan ending on "2026-03-01"
    Then the plan includes a taper block before "2026-03-01"
    And intensity is reduced during the taper while preserving key efforts

    Scenario: Event date change triggers plan rebase
    Given a published plan exists for a saved athlete with a goal event on "2026-03-01"
    When the athlete changes the event date to "2026-03-15"
    Then the plan is rebased to "2026-03-15"
    And the change history is preserved

