Feature: Authentication and session management

  @wip
  Scenario: Login with valid credentials returns tokens
    Given a user exists with username "coach_a" and role "COACH"
    When the user logs in with username "coach_a" and password "secret"
    Then an access token is issued
    And a refresh token is issued

  Scenario: Login fails for invalid credentials
    Given a user exists with username "coach_a" and role "COACH"
    When the user logs in with username "coach_a" and password "wrong"
    Then login is rejected with "UNAUTHORIZED"

  Scenario: Refresh rotates tokens
    Given a user exists with username "coach_a" and role "COACH"
    And a valid refresh token exists for the user
    When the user refreshes the session
    Then a new access token is issued
    And a new refresh token is issued
    And the previous refresh token is revoked

  Scenario: Logout revokes refresh token
    Given a user exists with username "coach_a" and role "COACH"
    And a valid refresh token exists for the user
    When the user logs out
    Then the refresh token is revoked

