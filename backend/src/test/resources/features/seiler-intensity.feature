Feature: Seiler intensity modeling (LT1/LT2, 3-zone, polarized distribution)

  Background:
    Given a coach user exists

  Scenario: Establish LT1 and LT2 for an athlete using a test protocol
    Given a saved athlete
    When the coach records LT1 watts 180.0 and LT2 watts 260.0 effective date "2026-01-01" method "LAB_LACTATE"
    Then the athlete has LT1 watts 180.0 and LT2 watts 260.0
    And the athlete has 3-zone boundaries derived from LT1 and LT2

  Scenario: Classify a session into Z1/Z2/Z3 using thresholds
    Given a saved athlete with LT1 watts 180.0 and LT2 watts 260.0
    When the athlete completes a session of 60 minutes with time in zones Z1 50 Z2 5 Z3 5
    Then the session is classified as polarized
    And zone "Z2" share is below 10 percent

  Scenario: FATMAX is a target band inside Z1 (below LT1)
    Given a saved athlete with LT1 watts 180.0 and LT2 watts 260.0
    When the coach prescribes a "FATMAX" endurance session for 60 minutes
    Then the prescribed target intensity is below LT1
    And the prescription is labeled as "Z1_FATMAX"
    And the prescription includes method and confidence

  Scenario: Detect Z2 creep over a week
    Given a saved athlete with LT1 watts 180.0 and LT2 watts 260.0
    And the athlete has 7 days of sessions with zone distribution
    When weekly time in zone is computed
    Then the system flags "Z2_CREEP" when zone "Z2" exceeds 20 percent

  Scenario: Plan generation uses polarized constraints based on LT1/LT2 zones
    Given a saved athlete with LT1 watts 180.0 and LT2 watts 260.0
    And athlete availability "MONDAY,WEDNESDAY,FRIDAY" weekly volume 8.0 phase "base"
    When the coach generates a plan ending on "2026-01-28"
    Then the plan targets zone "Z1" at least 75 percent of planned time
    And the plan targets zone "Z3" around 20 percent of planned time
    And the plan targets zone "Z2" at most 10 percent of planned time
    And high intensity work is prescribed as "VO2_OPTIMAL" rather than "SPRINT" by default

  Scenario: VO2-optimal vs sprint classification using %FTP
    Given a saved athlete with FTP 250.0
    When the coach prescribes an interval session at 110 percent of FTP for 5 minutes repeats
    Then the session target is classified as "VO2_OPTIMAL"
    And the session target is classified as zone "Z3"
    When the coach prescribes an interval session at 130 percent of FTP for 20 seconds repeats
    Then the session target is classified as "SPRINT"
    And the classification includes method and confidence

  Scenario: Adjustment reduces intensity distribution when readiness is low
    Given a published plan exists for a saved athlete
    And the athlete has LT1 watts 180.0 and LT2 watts 260.0
    And the athlete has readiness below 40 for 3 consecutive days
    When the coach requests an adjustment
    Then high-intensity dose in zone "Z3" is reduced first
    And low-intensity volume in zone "Z1" is preserved where possible
    And sprint work is removed before VO2-optimal work
