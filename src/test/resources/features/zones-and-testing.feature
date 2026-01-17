Feature: Testing and training zones

  Background:
    Given a coach user exists

  Scenario: Athlete completes an FTP test and zones are updated
    Given a saved athlete with FTP 220.0
    When the athlete completes an FTP test with result 240.0 on "2026-01-10"
    Then the athlete FTP is updated to 240.0
    And LT1 and LT2 proxies are updated where applicable
    And Seiler 3-zone boundaries are recalculated
    And prescription bands include method and confidence

  Scenario: Updated FTP affects future prescriptions only
    Given a published plan exists for a saved athlete
    And the athlete FTP is updated from 220.0 to 240.0 on "2026-01-10"
    When the athlete views a workout scheduled on "2026-01-12"
    Then workout targets reflect FTP 240.0
    And completed workouts before "2026-01-10" remain unchanged
