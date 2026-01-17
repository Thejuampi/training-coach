Feature: Admin workflows

  Background:
    Given an admin user exists

  Scenario: UC8 Configure Integrations
    Given an admin enters API key "test-key"
    When the integration is validated
    Then integration status is "active"

  Scenario: F1 Identity - create a coach user
    When the admin creates a user named "Coach A" with role "COACH"
    Then the user list contains a user named "Coach A" with role "COACH"

  Scenario: F1 Identity - create an athlete user with default preferences
    When the admin creates a user named "Athlete A" with role "ATHLETE"
    Then the user "Athlete A" has measurement system "METRIC"

  Scenario: F1 Identity - change user preferences
    Given a user named "Athlete A" exists with role "ATHLETE"
    When the admin sets user "Athlete A" distance unit to "KILOMETERS"
    Then the user "Athlete A" preferences reflect distance unit "KILOMETERS"

  Scenario: F1 Identity - view credential status without exposing secrets
    Given a user named "Coach A" exists with role "COACH"
    And credentials exist for user "Coach A" with username "coach_a"
    When the admin views credential status for "Coach A"
    Then the credential status is "enabled"
    And the password hash is not exposed

  Scenario: F3 Integrations - integration health is visible without exposing secrets
    Given an integration is configured
    When the admin views integration status
    Then the integration status is shown as "active"
    And the API key is not displayed

  Scenario: UC9 Monitor integration health over time
    Given an integration with "Intervals.icu" has been configured
    And the integration has experienced multiple sync failures over the past 24 hours
    When the admin views integration health dashboard
    Then the admin sees the integration status as "degraded"
    And the admin sees a history of sync events with timestamps

  Scenario: UC9 Integration degradation triggers alert
    Given an integration is configured
    And the integration has 3 consecutive failed sync attempts
    When the admin checks notifications
    Then the admin sees an alert about the degraded integration
    And the alert includes error details and remediation steps

  Scenario: F15 Reports - admin exports an organization summary
    Given multiple athletes exist with recent activity and wellness data
    When the admin exports an organization report for date range "2026-01-01" to "2026-01-07"
    Then the report includes athlete readiness coverage and compliance summary

  Scenario: F20 Privacy - delete athlete data on request
    Given an athlete exists with stored activities wellness and notes
    When the admin deletes the athlete and all associated personal data
    Then the athlete cannot be found
    And all associated data is removed
