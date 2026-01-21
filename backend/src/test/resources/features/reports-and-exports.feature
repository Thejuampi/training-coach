@wip
Feature: Reports and exports

  Background:
    Given a coach user exists

  @wip
  Scenario: Weekly report includes readiness and compliance trends
    Given a published plan exists for a saved athlete
    And completed activities and wellness are available for date range "2026-01-01" to "2026-01-07"
    When the coach generates a weekly report for that date range
    Then the report includes readiness trend
    And the report includes compliance summary
    And the report includes key notes

  @wip
  Scenario: Export report as CSV
    Given a weekly report exists for a saved athlete
    When the coach exports the report as "CSV"
    Then a CSV export is produced with stable column names

  @wip
  Scenario: Export report as JSON
    Given a weekly report exists for a saved athlete
    When the coach exports the report as "JSON"
    Then a JSON export is produced containing the full report structure

