Feature: Notifications and reminders

  Background:
    Given the system is running

  Scenario: Daily workout reminder is sent
    Given a published plan exists for a saved athlete
    And the athlete has a planned workout tomorrow
    When the daily notification job runs
    Then the athlete receives a workout reminder

  Scenario: Missed key session triggers coach alert
    Given a published plan exists for a saved athlete
    And the athlete missed a key session this week
    When the weekly summary job runs
    Then the coach receives an alert for the missed key session

  Scenario: Low readiness streak triggers fatigue warning
    Given a saved athlete has readiness below 40 for 3 consecutive days
    When the daily notification job runs
    Then the athlete receives a fatigue warning
    And the coach receives a fatigue notification

