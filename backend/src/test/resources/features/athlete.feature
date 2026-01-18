@smoke
Feature: Athlete workflows

  Background:
    Given an athlete user exists

  @smoke
  Scenario: UC4 Review Readiness & Wellness
    Given readiness signals hrv 60.0 rhr 50.0 sleep 7.0 fatigue 3 subjective 6
    When readiness is calculated
    Then readiness score is between 0 and 100

  Scenario: F5 Wellness - submit daily wellness entry
    Given a saved athlete
    When the athlete submits wellness for date "2026-01-02"
      And fatigue score 3 stress score 3 sleep quality score 7 motivation score 7 soreness score 2
      And resting heart rate 50.0 hrv 60.0 sleep hours 7.5 body weight kg 70.0
      And notes "Felt good"
    Then the latest wellness snapshot for the athlete has date "2026-01-02"
    And readiness score is between 0 and 100

  Scenario: F5 Wellness - edit a wellness entry
    Given a saved athlete
    And the athlete submitted wellness for date "2026-01-02"
    When the athlete edits wellness for date "2026-01-02" and sets fatigue score 6
    Then the wellness snapshot for date "2026-01-02" reflects fatigue score 6
    And readiness is recomputed

  @smoke
  Scenario: UC5 Track Compliance/Progress
    Given a plan with 3 workouts planned
    And actual workouts completed 2
    When compliance is evaluated
    Then compliance percent is 66.67

  Scenario: F4 Sync + F8 Compliance - match a completed activity to a planned workout
    Given a published plan exists for a saved athlete
    And the plan contains a workout on "2026-01-03" of type "ENDURANCE" duration minutes 90
    When the athlete completes an activity on "2026-01-03" duration minutes 95
    And activities are synced
    Then the activity is matched to the planned workout on "2026-01-03"
    And compliance for that workout is within tolerance

  Scenario: F6 Plan - view today's workout
    Given a published plan exists for a saved athlete
    When the athlete opens the plan for date "2026-01-03"
    Then the athlete sees workout type and target duration
    And the athlete sees intensity guidance based on their zones

  Scenario: F7 Workout execution - add post-workout feedback
    Given a published plan exists for a saved athlete
    And the athlete completed the planned workout on "2026-01-03"
    When the athlete logs RPE 7 and notes "Hard headwind"
    Then the feedback is stored and visible to the coach

  @smoke
  Scenario: UC9 Review Activity History
    Given a saved athlete with linked Intervals.icu
    When activities are ingested from "2026-01-01" to "2026-01-03"
    Then activity history is available

  Scenario: F12 Availability - update availability template
    Given a saved athlete
    When the athlete updates availability to "MONDAY,TUESDAY,THURSDAY" with weekly volume hours 7.0
    Then future plan generation uses the updated availability

  Scenario: UC19 Update measurement units
    Given a saved athlete with measurement system "METRIC"
    When the athlete updates distance unit to "MILES"
      And updates weight unit to "POUNDS"
    Then the athlete preferences reflect distance unit "MILES"
    And the athlete preferences reflect weight unit "POUNDS"

  Scenario: UC19 Adjust privacy settings
    Given a saved athlete with default privacy settings
    When the athlete sets activity visibility to "PRIVATE"
      And sets wellness data sharing to "COACH_ONLY"
    Then the athlete privacy settings are updated
    And activity data is only visible to the athlete and coach

  Scenario: UC19 Settings conflict with plan triggers notification
    Given a published plan exists for a saved athlete
    And the athlete updates weekly volume hours to 5.0
    When the athlete saves the changes
    Then the coach is notified of the settings change
    And the notification indicates a potential conflict with the current plan

  Scenario: F13 Events - athlete adds a goal race
    Given a saved athlete
    When the athlete adds a goal event "Spring Classic" on "2026-03-01" priority "A"
    Then the event appears on the athlete calendar

  Scenario: F10 Communication - read coach notes
    Given a saved athlete
    And the coach posted a note "Focus on recovery this week"
    When the athlete views notes
    Then the athlete sees the note "Focus on recovery this week"
