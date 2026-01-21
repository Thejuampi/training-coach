@wip
Feature: Availability, calendar, and exceptions

  Background:
    Given an athlete user exists

  @wip
  Scenario: Athlete updates weekly availability template
    Given a saved athlete with availability "MONDAY,WEDNESDAY,FRIDAY"
    When the athlete updates availability to "TUESDAY,THURSDAY,SATURDAY" and weekly volume hours 7.0
    Then future plan generation uses "TUESDAY,THURSDAY,SATURDAY"

  @wip
  Scenario: Athlete adds a travel exception and workouts are flagged
    Given a published plan exists for a saved athlete
    When the athlete adds an availability exception from "2026-01-15" to "2026-01-17" type "TRAVEL"
    Then workouts scheduled during "2026-01-15" to "2026-01-17" are flagged as conflicted

  @wip
  Scenario: Coach applies an auto-reschedule within guardrails
    Given conflicted workouts exist for a saved athlete
    When the coach applies auto-reschedule for the conflicted workouts
    Then workouts are moved to available days within the same week
    And the weekly volume remains within safety caps

