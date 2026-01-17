Feature: Plan lifecycle (draft, publish, revise, archive)

  Background:
    Given a coach user exists

  Scenario: Draft generation produces a draft plan
    Given a saved athlete with availability "MONDAY,WEDNESDAY,FRIDAY" weekly volume 8.0 phase "base"
    When the coach generates a plan draft for phase "base" start date "2026-01-01" target weekly hours 8.0 duration weeks 4
    Then a draft plan exists
    And the draft plan has workouts assigned on athlete available days

  Scenario: Publishing a plan makes it visible to athlete
    Given a draft plan exists for a saved athlete
    When the coach publishes the draft plan
    Then the athlete can view the plan as "published"
    And the plan has a publish timestamp

  Scenario: Plan revision creates a new version
    Given a published plan exists for a saved athlete
    When the coach revises the plan and reduces weekly hours by 1.0
    Then the plan has a new version
    And previous versions remain accessible

  Scenario: Archive a plan at end of cycle
    Given a published plan exists that ended on "2026-02-01"
    When the coach archives the plan
    Then the plan is marked "archived"
    And the plan is excluded from active plan lists

