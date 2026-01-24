Feature: Multi-platform reconciliation

  Background:
    Given the system is running

    Scenario: Detect and merge duplicate activities
    Given an athlete has duplicate activities from two platforms
    When reconciliation runs
    Then duplicate activities are merged into a single canonical record
    And the source of truth is recorded

    Scenario: Conflicting data requires review
    Given an athlete has conflicting activity data across platforms
    When reconciliation runs
    Then the activity is flagged for manual review
    And no data is lost from either source

    Scenario: Apply precedence rules
    Given platform "A" is configured as the source of truth
    And platform "B" submits overlapping activities
    When reconciliation runs
    Then platform "A" data is retained
    And platform "B" data is attached as provenance
