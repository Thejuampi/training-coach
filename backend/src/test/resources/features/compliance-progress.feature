Feature: Compliance and progress tracking

  Background:
    Given a coach user exists

  Scenario: Weekly compliance includes key sessions
    Given a published plan exists for a saved athlete
    And completed activities are synced for date range "2026-01-01" to "2026-01-07"
    When weekly compliance is computed for that date range
    Then compliance includes completion percent
    And compliance includes key session completion
    And compliance includes Seiler 3-zone distribution adherence
    And compliance flags "Z2_CREEP" when zone "Z2" is too high

  Scenario: Ad-hoc activity can be classified
    Given a published plan exists for a saved athlete
    And an activity exists on "2026-01-05" with no matching planned workout
    When the coach classifies the activity as "ad_hoc"
    Then compliance metrics include the activity as unplanned load

  Scenario: Progress summary shows load trend and streaks
    Given a saved athlete has 8 weeks of synced activities and wellness
    When the coach opens the progress summary
    Then the coach sees weekly volume trend
    And the coach sees training load trend
    And the coach sees completion streaks
