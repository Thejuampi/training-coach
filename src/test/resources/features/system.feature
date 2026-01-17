Feature: System workflows

  Background:
    Given the system is running

  Scenario: UC2 Sync Athlete Data
    Given a saved athlete with linked Intervals.icu
    When a sync is triggered
    Then workouts and wellness are ingested
    And a sync event is recorded

  Scenario: F4 Sync - scheduled sync runs nightly
    Given multiple athletes are linked to Intervals.icu
    When the nightly sync job runs
    Then each athlete is synced
    And failures are recorded per athlete

  Scenario: F4 Sync - partial failure preserves partial success
    Given a saved athlete with linked Intervals.icu
    And the platform activities endpoint is healthy
    And the platform wellness endpoint fails
    When a sync is triggered
    Then activities are ingested
    And wellness remains stale
    And the sync run is marked "partial_failure"

  Scenario: F13 Notifications - remind athlete to submit wellness
    Given a saved athlete has not submitted wellness for 3 days
    When the daily reminder job runs
    Then the athlete receives a wellness reminder notification

  Scenario: UC18 Detect conflicts between multiple data sources
    Given a saved athlete is linked to both "Intervals.icu" and "Strava"
    And the athlete has an activity on "2026-01-03" with duration 60 minutes from "Intervals.icu"
    And the athlete has an activity on "2026-01-03" with duration 65 minutes from "Strava"
    When the system processes the sync
    Then a conflict is detected between the two activities
    And the conflict is flagged for review

  Scenario: UC18 Apply precedence rules to resolve conflicts
    Given a saved athlete is linked to both "Intervals.icu" and "Strava"
    And "Intervals.icu" is configured with higher precedence
    And conflicting activities exist from both platforms on "2026-01-03"
    When the system applies precedence rules
    Then the "Intervals.icu" activity is selected as the canonical record
    And the "Strava" activity is marked as duplicate

  Scenario: UC18 Flag ambiguous conflicts for manual review
    Given a saved athlete is linked to multiple platforms
    And the system detects activities with similar but not identical timestamps and durations
    When the system cannot automatically determine precedence
    Then the activities are flagged as "ambiguous"
    And the admin is notified of the pending review
    And the admin can manually select the canonical record

  Scenario: UC18 Merge canonical record after manual review
    Given activities are flagged as "ambiguous" for manual review
    When the admin selects the correct activity as canonical
    Then the selected activity becomes the canonical record
    And other conflicting activities are marked as duplicates
    And the decision is logged in the audit trail

  Scenario: F16 Safety - enforce weekly ramp rate cap
    Given a saved athlete has a current weekly training load of 300 TSS
    When a plan adjustment increases next week's load to 420 TSS
    Then the system blocks the adjustment
    And a safety violation is recorded
