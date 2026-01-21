@wip
Feature: Workout library (templates, tagging, and reuse)

  Background:
    Given a coach user exists

  @wip
  Scenario: UC17 - Create a workout template and tag by purpose
    Given a coach wants to create a workout template
    When the coach defines an interval session with name "5x5min VO2"
      And sets duration to 60 minutes
      And sets type to "INTERVALS"
      And tags the purpose as "VO2_OPTIMAL"
      And tags the intensity zone as "Z3"
    Then a workout template is created
    And the template is saved to the coach's library

  @wip
  Scenario: Create an endurance workout template with FATMAX target
    Given a coach wants to create an endurance session template
    When the coach defines a session with name "90min Endurance"
      And sets duration to 90 minutes
      And sets type to "ENDURANCE"
      And tags the purpose as "FATMAX"
      And tags the intensity zone as "Z1"
    Then a workout template is created
    And the template includes the FATMAX target band specification

  @wip
  Scenario: Tag a workout template by phase and priority
    Given a workout template "5x5min VO2" exists
    When the coach tags the template with phase "build"
      And tags the template with priority "KEY_SESSION"
    Then the template metadata reflects phase "build"
    And the template is marked as a "KEY_SESSION"

  @wip
  Scenario: Publish a workout template for reuse
    Given a workout template is in draft status
    When the coach publishes the template
    Then the template is marked as "published"
    And the template is available for insertion into plans

  @wip
  Scenario: Insert a workout template into a plan draft
    Given a published workout template "5x5min VO2" exists
    And a draft plan exists for a saved athlete
    When the coach inserts the template into the plan on date "2026-01-08"
    Then the plan contains a workout on "2026-01-08"
    And the workout inherits the template's structure
    And the workout inherits the template's tags

  @wip
  Scenario: Modify a workout template creates a new version
    Given a published workout template "5x5min VO2" exists
    When the coach modifies the template to change duration from 60 to 65 minutes
    Then a new template version is created
    And previous versions remain accessible
    And the new version becomes the default

  @wip
  Scenario: Deprecate a workout template
    Given a published workout template "5x5min VO2" exists
    When the coach deprecates the template
    Then the template is marked as "deprecated"
    And the template is not shown in available templates for new plans

  @wip
  Scenario: Filter workout library by purpose and phase
    Given the coach has multiple workout templates in the library
      And template "5x5min VO2" is tagged with purpose "VO2_OPTIMAL" and phase "build"
      And template "90min Endurance" is tagged with purpose "FATMAX" and phase "base"
    When the coach filters the library by purpose "VO2_OPTIMAL"
    Then only templates tagged with "VO2_OPTIMAL" are displayed
    When the coach filters the library by phase "base"
    Then only templates tagged with "base" are displayed

  @wip
  Scenario: Copy a workout template from another coach's library
    Given a coach has sharing enabled for their workout library
      And coach "Coach A" has a published template "Turbo Intervals"
    When coach "Coach B" views shared templates
      And copies "Turbo Intervals" to their own library
    Then "Coach B" has a copy of "Turbo Intervals"
    And the original template remains owned by "Coach A"

