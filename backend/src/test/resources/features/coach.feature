@smoke
Feature: Coach workflows

  Background:
    Given a coach user exists

  @smoke
  Scenario: UC1 Manage Athlete
    Given a coach creates an athlete profile with age 30 and level intermediate
    And metrics ftp 250.0 fthr 180.0 vo2 45.0
    And preferences availability "MONDAY,WEDNESDAY,FRIDAY" weekly volume 10.0 phase "base"
    When the coach saves the athlete profile
    Then the athlete is stored with level "intermediate"

  @wip
  Scenario: UC1 Link external platform to athlete
    Given a saved athlete "Athlete A" exists
    When the coach links athlete "Athlete A" to "Intervals.icu"
    Then the athlete has an integration with "Intervals.icu"
    And the integration status is "active"

  @wip
  Scenario: UC1 Request testing protocol when metrics are missing
    Given a coach creates an athlete profile with age 30 and level intermediate
    And metrics are empty
    When the coach saves the athlete profile
    Then the system recommends requesting a testing protocol
    And the coach sees available test protocols for FTP and threshold tests

  Scenario: UC3 Generate Training Plan
    Given a saved athlete with availability "MONDAY,WEDNESDAY,FRIDAY" weekly volume 10.0 phase "base"
    When the coach requests a plan for phase "base" start date "2026-01-01" target weekly hours 10.0
    Then the plan targets a polarized 3-zone distribution
    And planned training time in zone "Z2" is minimized

  Scenario: F6 Plan lifecycle - publish a plan
    Given a saved athlete with availability "MONDAY,WEDNESDAY,FRIDAY" weekly volume 8.0 phase "base"
    And a plan draft exists for start date "2026-01-01" duration weeks 4
    When the coach publishes the plan
    Then the athlete can view the published plan
    And the plan has a version id and publish timestamp

  Scenario: F6 Plan lifecycle - revise a plan and keep history
    Given a published plan exists for a saved athlete
    When the coach applies an adjustment to reduce weekly volume by 10 percent
    Then a new plan version is created
    And the previous plan version remains viewable

  @smoke
  Scenario: UC6 Adjust Plan
    Given readiness score 3.0 and compliance 90.0
    When the coach asks for an adjustment
    Then the recommendation suggests reducing volume

  @wip
  Scenario: F9 Adjustments - swap key session within guardrails
    Given a published plan exists for a saved athlete
    And the athlete has low readiness for 2 consecutive days
    When the coach swaps tomorrow's intervals session with a recovery session
    Then the plan remains valid under safety guardrails
    And the athlete is notified of the change

  @wip
  Scenario: F8 Compliance - weekly review summary
    Given a published plan exists for a saved athlete
    And completed activities are synced for the last 7 days
    When the coach opens the weekly compliance summary
    Then the coach sees completion rate for planned workouts
    And the coach sees intensity distribution versus target 80/20
    And the coach sees flags for missed key sessions

  @smoke
  Scenario: UC7 Coach Communication
    Given a saved athlete
    When the coach posts a note "Focus on recovery this week"
    Then the athlete has 1 note stored

  @wip
  Scenario: F10 Communication - note linked to a specific date
    Given a saved athlete
    And the athlete has a planned workout on "2026-01-03"
    When the coach posts a note "Stay in Z2 today" linked to date "2026-01-03"
    Then the note appears in the athlete's context for date "2026-01-03"

  @wip
  Scenario: F11 Testing & zones - schedule FTP test
    Given a saved athlete with unknown FTP
    When the coach schedules an FTP ramp test for date "2026-01-10"
    Then the athlete sees the test on their calendar
    And the athlete is guided on how to execute the test

  @wip
  Scenario: F12 Availability - add travel exception and replan
    Given a published plan exists for a saved athlete
    When the coach adds a travel exception from "2026-01-15" to "2026-01-17"
    Then conflicting workouts are flagged
    And the coach can apply an auto-reschedule within the same week

  @wip
  Scenario: F13 Events - add a priority race and include taper
    Given a saved athlete
    When the coach adds an "A" priority race on "2026-03-01"
    And the coach generates a plan ending on "2026-03-01"
    Then the plan includes a taper block before "2026-03-01"
    And the plan preserves intensity distribution within guardrails

  @wip
  Scenario: F15 Reports - export weekly report
    Given a published plan exists for a saved athlete
    And completed activities are synced for the last 7 days
    When the coach exports the weekly report as "CSV"
    Then a report file is generated containing compliance and readiness trends

  @wip
  Scenario: F16 Safety - block intensity when fatigue is high
    Given a published plan exists for a saved athlete
    And the athlete reports fatigue score 9 and soreness score 9 for today
    When the coach attempts to schedule intervals for tomorrow
    Then the system blocks the change
    And the coach sees the blocking rule and safe alternatives
