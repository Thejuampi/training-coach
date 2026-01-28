Feature: UI views and navigation

  Background:
    Given the application is running

  Scenario: SplashView shows backend connectivity
    When the app starts
    Then the splash screen shows backend status "connected"
    And the session view is available

  Scenario: SplashView handles unreachable backend
    Given the backend is unreachable
    When the app starts
    Then the splash screen shows backend status "unreachable"
    And the user can open connection settings

  Scenario: ConnectionSettingsView updates backend URL
    Given the connection settings are open
    When the user sets the backend URL to "http://localhost:8080"
    And the user tests the connection
    Then the connection test succeeds
    And the backend URL is saved for the session

  Scenario: MainMenuView shows role-based navigation
    Given the user role is "COACH"
    When the main menu is displayed
    Then menu items include "Athletes"
    And menu items include "Plans"
    And menu items include "Tools"

  Scenario: AthleteDeleteConfirmView requires confirmation
    Given an athlete exists
    And the user opens the athlete delete dialog
    When the user confirms deletion
    Then the athlete is deleted
    And a success message is shown

  Scenario: WellnessDeleteConfirmView requires confirmation
    Given a wellness entry exists for date "2026-01-05"
    And the user opens the wellness delete dialog
    When the user cancels the deletion
    Then the wellness entry remains unchanged

  Scenario: WellnessReportView generates a report
    Given wellness data exists for date range "2026-01-01" to "2026-01-07"
    When the user requests a wellness report for that date range
    Then a wellness report is shown
    And the report includes readiness trend and notes

  Scenario: RecoveryRecommendationsView shows recommendations
    Given wellness data exists for the last 7 days
    When the user opens recovery recommendations
    Then recommendations are shown for the athlete

  Scenario: PlanListView shows placeholder when backend is not ready
    Given the backend does not support plan persistence
    When the user opens the saved plans list
    Then the UI shows "Plan list not available yet"
    And the user can return to the plan generator

  Scenario: PlanDetailView shows placeholder when plan retrieval is missing
    Given the backend does not support plan retrieval
    When the user opens a saved plan detail
    Then the UI shows "Plan detail not available yet"
    And the user can return to the plan list

  Scenario: TrendToolView runs ad-hoc analysis
    Given activity data exists for date range "2026-01-01" to "2026-01-14"
    When the user runs a trend analysis for that date range
    Then a trend summary is shown
    And the user can export the trend summary

  Scenario: ErrorDetailsView shows diagnostics with redaction
    Given a request failed with status 500
    When the user opens error details
    Then the request path and status are shown
    And secrets are redacted from the diagnostics

  Scenario: KeybindingsView lists shortcuts
    When the user opens keybindings help
    Then keyboard shortcuts for navigation are listed
    And keyboard shortcuts for refresh are listed

  Scenario: AboutView shows app metadata
    When the user opens the about view
    Then the app version is shown
    And a link to documentation is shown

  Scenario: NotificationsView shows background refresh status
    Given a background refresh completed with warnings
    When the user opens notifications
    Then the latest notification is shown
    And the user can update notification preferences

  Scenario: SyncStatusView shows progress placeholder
    Given sync history is not yet exposed by the backend
    When the user opens sync status
    Then the UI shows "Sync history not available yet"
    And the user can trigger a manual sync

  Scenario: AiSuggestionView keeps history
    Given the user opens the AI suggestions view
    When the user submits prompt "Explain Z2 creep"
    Then the AI response is shown
    And the prompt and response are stored in local history

  Scenario: UserPreferencesView saves changes
    Given a user preferences view is open
    When the user sets distance units to "KM"
    Then the preferences are saved
    And the change is reflected in the session

  Scenario: AthleteSwitchView changes current athlete
    Given a coach has two athletes
    When the coach switches to athlete "A-2"
    Then the current athlete context is "A-2"
    And athlete-specific menus update

  Scenario: HelpView shows onboarding guidance
    When the user opens help
    Then a getting started guide is shown
    And contact/support information is shown
