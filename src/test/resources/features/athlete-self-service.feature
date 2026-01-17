Feature: Athlete self-service settings

  Background:
    Given an athlete user exists

  Scenario: Update units and preferences
    When the athlete sets distance units to "KM"
    And the athlete sets weight units to "KG"
    Then the preferences are saved
    And future forms default to the chosen units

  Scenario: Update privacy settings
    When the athlete disables data sharing with external platforms
    Then sync is paused for that athlete
    And the coach is notified of the privacy change
