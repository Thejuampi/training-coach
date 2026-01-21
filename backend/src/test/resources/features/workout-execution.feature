@wip
Feature: Workout execution and feedback

  Background:
    Given an athlete user exists

  @wip
  Scenario: Athlete completes planned workout and it is matched
    Given a published plan exists for a saved athlete
    And the plan contains a workout on "2026-01-03" of type "ENDURANCE" duration minutes 90
    When the athlete completes an activity on "2026-01-03" duration minutes 92
    And activities are synced
    Then the activity is matched to the planned workout on "2026-01-03"

  @wip
  Scenario: VO2-optimal intervals are prescribed differently from sprint work
    Given a published plan exists for a saved athlete
    And the athlete has FTP 250.0
    When the athlete views a planned "VO2_OPTIMAL" interval session
    Then the target is between 105 and 115 percent of FTP
    And the target includes method and confidence
    When the athlete views a planned "SPRINT" session
    Then the target is above 115 percent of FTP
    And the target includes method and confidence

  @wip
  Scenario: Athlete logs post-workout RPE and notes
    Given a saved athlete
    And the athlete completed an activity on "2026-01-03"
    When the athlete logs RPE 7 and notes "Felt strong"
    Then the coach can view the workout feedback for "2026-01-03"

  @wip
  Scenario: Athlete skips a key session and provides a reason
    Given a published plan exists for a saved athlete
    And the plan contains a workout on "2026-01-04" of type "INTERVALS"
    When the athlete marks the workout on "2026-01-04" as skipped with reason "sick"
    Then the skip reason is visible to the coach
    And the system suggests a safe recovery option
