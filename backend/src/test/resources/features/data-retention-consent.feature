@wip
Feature: Data retention and consent

  Background:
    Given an admin user exists

  @wip
  Scenario: Export athlete data on request
    Given an athlete requests a data export
    When the admin approves the export request
    Then a data export archive is generated
    And the export action is recorded in the consent log

  @wip
  Scenario: Delete athlete data on request
    Given an athlete requests data deletion
    When the admin approves the deletion request
    Then athlete data is deleted or anonymized
    And the deletion action is recorded in the consent log
