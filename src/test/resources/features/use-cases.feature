Feature: Training Coach use cases

  Scenario: UC1 Manage Athlete
    Given a coach creates an athlete profile with age 30 and level intermediate
    And metrics ftp 250.0 fthr 180.0 vo2 45.0
    And preferences availability "MONDAY,WEDNESDAY,FRIDAY" weekly volume 10.0 phase "base"
    When the coach connects the athlete to Intervals.icu
    Then the athlete is stored with link status "connected"

  Scenario: UC2 Sync Athlete Data
    Given an athlete with linked Intervals.icu
    When a sync is triggered
    Then workouts and wellness are ingested
    And a sync event is recorded

  Scenario: UC3 Generate Training Plan
    Given a coach creates an athlete profile with age 30 and level intermediate
    And metrics ftp 250.0 fthr 180.0 vo2 45.0
    And preferences availability "MONDAY,WEDNESDAY,FRIDAY" weekly volume 10.0 phase "base"
    When the coach requests a plan for phase "base" duration 4 weeks start date "2026-01-01" target weekly hours 10.0
    Then a plan is created with 80/20 intensity split

  Scenario: UC4 Review Readiness & Wellness
    Given readiness signals hrv 60.0 rhr 50.0 sleep 7.0 fatigue 20.0 subjective 3
    When readiness is calculated
    Then readiness score is between 0 and 100

  Scenario: UC5 Track Compliance/Progress
    Given a plan with 3 workouts planned
    And actual workouts completed 2
    When compliance is evaluated
    Then compliance percent is 0.66

  Scenario: UC6 Adjust Plan
    Given low readiness score 30.0
    When the coach applies a recovery adjustment
    Then weekly volume is reduced by 20 percent

  Scenario: UC7 Coach Communication
    Given an athlete and a plan exist
    When the coach posts a note "Focus on recovery this week"
    Then the note is stored for the athlete

  Scenario: UC8 Configure Integrations
    Given an admin enters API key "test-key"
    When the integration is validated
    Then integration status is "active"
