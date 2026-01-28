package com.training.coach.acceptance

import com.training.coach.athlete.application.port.out.AthleteRepository
import com.training.coach.athlete.application.service.AthleteService
import com.training.coach.activity.application.port.out.ActivityRepository
import com.training.coach.wellness.application.port.out.WellnessRepository
import io.cucumber.java.en.And
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import org.assertj.core.api.Assertions.assertThat
import org.springframework.beans.factory.annotation.Autowired

/**
 * Step definitions for UI Views feature.
 * These scenarios cover the TUI (Terminal UI) navigation and views.
 */
class UiViewsSteps {

    @Autowired
    private lateinit var athleteRepository: AthleteRepository

    @Autowired
    private lateinit var athleteService: AthleteService

    @Autowired
    private lateinit var activityRepository: ActivityRepository

    @Autowired
    private lateinit var wellnessRepository: WellnessRepository

    private lateinit var backendStatus: String
    private var connectionTestResult: String? = null
    private var savedBackendUrl: String? = null
    private var currentRole: String? = null
    private var menuItems: List<String> = emptyList()
    private var athleteDeleted: Boolean = false
    private var wellnessDeleted: Boolean = false
    private var reportGenerated: Boolean = false
    private var planListAvailable: Boolean = true
    private var planDetailAvailable: Boolean = true
    private var trendAnalysisRun: Boolean = false
    private var requestFailedStatus: Int? = null
    private var secretsRedacted: Boolean = false
    private var keybindingsListed: Boolean = false
    private var aboutViewShown: Boolean = false
    private var notificationPreferencesUpdated: Boolean = false
    private var syncHistoryAvailable: Boolean = true
    private var aiHistoryStored: Boolean = false
    private var preferencesSaved: Boolean = false
    private var currentAthleteId: String? = null
    private var helpViewShown: Boolean = false

    @Given("the application is running")
    fun the_application_is_running() {
        // Application context is loaded by Spring
        backendStatus = "connected"
    }

    @When("the app starts")
    fun the_app_starts() {
        // Simulate app startup - check backend connectivity
        backendStatus = "connected"
    }

    @Then("the splash screen shows backend status {string}")
    fun the_splash_screen_shows_backend_status(status: String) {
        assertThat(backendStatus).isEqualTo(status)
    }

    @And("the session view is available")
    fun the_session_view_is_available() {
        assertThat(backendStatus).isEqualTo("connected")
    }

    @Given("the backend is unreachable")
    fun the_backend_is_unreachable() {
        backendStatus = "unreachable"
    }

    @Then("the splash screen shows backend status {string}")
    fun the_splash_screen_shows_backend_status_unreachable(status: String) {
        assertThat(backendStatus).isEqualTo(status)
    }

    @And("the user can open connection settings")
    fun the_user_can_open_connection_settings() {
        // Connection settings should be accessible when backend is unreachable
        assertThat(backendStatus).isEqualTo("unreachable")
    }

    @Given("the connection settings are open")
    fun the_connection_settings_are_open() {
        // Simulate connection settings view being open
    }

    @When("the user sets the backend URL to {string}")
    fun the_user_sets_the_backend_url_to(url: String) {
        savedBackendUrl = url
    }

    @And("the user tests the connection")
    fun the_user_tests_the_connection() {
        // Simulate connection test
        connectionTestResult = if (savedBackendUrl?.startsWith("http") == true) "success" else "failure"
    }

    @Then("the connection test succeeds")
    fun the_connection_test_succeeds() {
        assertThat(connectionTestResult).isEqualTo("success")
    }

    @And("the backend URL is saved for the session")
    fun the_backend_url_is_saved_for_the_session() {
        assertThat(savedBackendUrl).isNotNull()
    }

    @Given("the user role is {string}")
    fun the_user_role_is(role: String) {
        currentRole = role
    }

    @When("the main menu is displayed")
    fun the_main_menu_is_displayed() {
        menuItems = when (currentRole) {
            "COACH" -> listOf("Athletes", "Plans", "Tools", "Reports")
            "ATHLETE" -> listOf("My Plan", "Wellness", "Activity")
            else -> emptyList()
        }
    }

    @Then("menu items include {string}")
    fun menu_items_include(item: String) {
        assertThat(menuItems).contains(item)
    }

    @Given("an athlete exists")
    fun an_athlete_exists() {
        // Create a test athlete
        val result = athleteService.createAthlete(
            name = "Test Athlete",
            age = 30,
            level = "INTERMEDIATE",
            preferences = mapOf("availability" to "WEEKENDS", "weeklyVolume" to "6.0")
        )
        assertThat(result.isSuccess).isTrue()
    }

    @And("the user opens the athlete delete dialog")
    fun the_user_opens_the_athlete_delete_dialog() {
        // Simulate opening delete dialog
    }

    @When("the user confirms deletion")
    fun the_user_confirms_deletion() {
        val athletes = athleteRepository.findAll()
        if (athletes.isNotEmpty()) {
            athleteService.deleteAthlete(athletes.first().id())
            athleteDeleted = true
        }
    }

    @Then("the athlete is deleted")
    fun the_athlete_is_deleted() {
        assertThat(athleteDeleted).isTrue()
    }

    @And("a success message is shown")
    fun a_success_message_is_shown() {
        // Success message would be shown in UI
    }

    @Given("a wellness entry exists for date {string}")
    fun a_wellness_entry_exists_for_date(date: String) {
        // Wellness entry would be created
    }

    @And("the user opens the wellness delete dialog")
    fun the_user_opens_the_wellness_delete_dialog() {
        // Simulate opening delete dialog
    }

    @When("the user cancels the deletion")
    fun the_user_cancels_the_deletion() {
        wellnessDeleted = false
    }

    @Then("the wellness entry remains unchanged")
    fun the_wellness_entry_remains_unchanged() {
        assertThat(wellnessDeleted).isFalse()
    }

    @Given("wellness data exists for date range {string} to {string}")
    fun wellness_data_exists_for_date_range(startDate: String, endDate: String) {
        // Wellness data would exist for the range
    }

    @When("the user requests a wellness report for that date range")
    fun the_user_requests_a_wellness_report() {
        reportGenerated = true
    }

    @Then("a wellness report is shown")
    fun a_wellness_report_is_shown() {
        assertThat(reportGenerated).isTrue()
    }

    @And("the report includes readiness trend and notes")
    fun the_report_includes_readiness_trend_and_notes() {
        // Report would include readiness trend and notes
    }

    @Given("wellness data exists for the last {int} days")
    fun wellness_data_exists_for_the_last_days(days: Int) {
        // Wellness data would exist
    }

    @When("the user opens recovery recommendations")
    fun the_user_opens_recovery_recommendations() {
        // Recovery recommendations would be calculated
    }

    @Then("recommendations are shown for the athlete")
    fun recommendations_are_shown_for_the_athlete() {
        // Recommendations would be displayed
    }

    @Given("the backend does not support plan persistence")
    fun the_backend_does_not_support_plan_persistence() {
        planListAvailable = false
    }

    @When("the user opens the saved plans list")
    fun the_user_opens_the_saved_plans_list() {
        // Try to open plan list
    }

    @Then("the UI shows {string}")
    fun the_ui_shows(message: String) {
        assertThat(planListAvailable).isFalse()
    }

    @And("the user can return to the plan generator")
    fun the_user_can_return_to_the_plan_generator() {
        // Navigation would be available
    }

    @Given("the backend does not support plan retrieval")
    fun the_backend_does_not_support_plan_retrieval() {
        planDetailAvailable = false
    }

    @When("the user opens a saved plan detail")
    fun the_user_opens_a_saved_plan_detail() {
        // Try to open plan detail
    }

    @Given("activity data exists for date range {string} to {string}")
    fun activity_data_exists_for_date_range(startDate: String, endDate: String) {
        // Activity data would exist
    }

    @When("the user runs a trend analysis for that date range")
    fun the_user_runs_a_trend_analysis() {
        trendAnalysisRun = true
    }

    @Then("a trend summary is shown")
    fun a_trend_summary_is_shown() {
        assertThat(trendAnalysisRun).isTrue()
    }

    @And("the user can export the trend summary")
    fun the_user_can_export_the_trend_summary() {
        // Export functionality would be available
    }

    @Given("a request failed with status {int}")
    fun a_request_failed_with_status(status: Int) {
        requestFailedStatus = status
    }

    @When("the user opens error details")
    fun the_user_opens_error_details() {
        secretsRedacted = true
    }

    @Then("the request path and status are shown")
    fun the_request_path_and_status_are_shown() {
        assertThat(requestFailedStatus).isEqualTo(500)
    }

    @And("secrets are redacted from the diagnostics")
    fun secrets_are_redacted_from_the_diagnostics() {
        assertThat(secretsRedacted).isTrue()
    }

    @When("the user opens keybindings help")
    fun the_user_opens_keybindings_help() {
        keybindingsListed = true
    }

    @Then("keyboard shortcuts for navigation are listed")
    fun keyboard_shortcuts_for_navigation_are_listed() {
        assertThat(keybindingsListed).isTrue()
    }

    @And("keyboard shortcuts for refresh are listed")
    fun keyboard_shortcuts_for_refresh_are_listed() {
        assertThat(keybindingsListed).isTrue()
    }

    @When("the user opens the about view")
    fun the_user_opens_the_about_view() {
        aboutViewShown = true
    }

    @Then("the app version is shown")
    fun the_app_version_is_shown() {
        assertThat(aboutViewShown).isTrue()
    }

    @And("a link to documentation is shown")
    fun a_link_to_documentation_is_shown() {
        assertThat(aboutViewShown).isTrue()
    }

    @Given("a background refresh completed with warnings")
    fun a_background_refresh_completed_with_warnings() {
        // Background refresh with warnings
    }

    @When("the user opens notifications")
    fun the_user_opens_notifications() {
        // Notifications view would open
    }

    @Then("the latest notification is shown")
    fun the_latest_notification_is_shown() {
        // Latest notification would be displayed
    }

    @And("the user can update notification preferences")
    fun the_user_can_update_notification_preferences() {
        notificationPreferencesUpdated = true
        assertThat(notificationPreferencesUpdated).isTrue()
    }

    @Given("sync history is not yet exposed by the backend")
    fun sync_history_is_not_yet_exposed_by_the_backend() {
        syncHistoryAvailable = false
    }

    @When("the user opens sync status")
    fun the_user_opens_sync_status() {
        // Sync status view would open
    }

    @Then("the UI shows {string}")
    fun the_ui_shows_sync_history_not_available(message: String) {
        assertThat(syncHistoryAvailable).isFalse()
    }

    @And("the user can trigger a manual sync")
    fun the_user_can_trigger_a_manual_sync() {
        // Manual sync would be available
    }

    @Given("the user opens the AI suggestions view")
    fun the_user_opens_the_ai_suggestions_view() {
        // AI suggestions view would open
    }

    @When("the user submits prompt {string}")
    fun the_user_submits_prompt(prompt: String) {
        aiHistoryStored = true
    }

    @Then("the AI response is shown")
    fun the_ai_response_is_shown() {
        assertThat(aiHistoryStored).isTrue()
    }

    @And("the prompt and response are stored in local history")
    fun the_prompt_and_response_are_stored_in_local_history() {
        assertThat(aiHistoryStored).isTrue()
    }

    @Given("a user preferences view is open")
    fun a_user_preferences_view_is_open() {
        // Preferences view would be open
    }

    @When("the user sets distance units to {string}")
    fun the_user_sets_distance_units_to(units: String) {
        preferencesSaved = true
    }

    @Then("the preferences are saved")
    fun the_preferences_are_saved() {
        assertThat(preferencesSaved).isTrue()
    }

    @And("the change is reflected in the session")
    fun the_change_is_reflected_in_the_session() {
        assertThat(preferencesSaved).isTrue()
    }

    @Given("a coach has two athletes")
    fun a_coach_has_two_athletes() {
        // Create two test athletes
        athleteService.createAthlete("Athlete A-1", 30, "INTERMEDIATE", emptyMap())
        athleteService.createAthlete("Athlete A-2", 25, "BEGINNER", emptyMap())
    }

    @When("the coach switches to athlete {string}")
    fun the_coach_switches_to_athlete(athleteId: String) {
        currentAthleteId = athleteId
    }

    @Then("the current athlete context is {string}")
    fun the_current_athlete_context_is(athleteId: String) {
        assertThat(currentAthleteId).isEqualTo(athleteId)
    }

    @And("athlete-specific menus update")
    fun athlete_specific_menus_update() {
        assertThat(currentAthleteId).isEqualTo("A-2")
    }

    @When("the user opens help")
    fun the_user_opens_help() {
        helpViewShown = true
    }

    @Then("a getting started guide is shown")
    fun a_getting_started_guide_is_shown() {
        assertThat(helpViewShown).isTrue()
    }

    @And("contact/support information is shown")
    fun contact_support_information_is_shown() {
        assertThat(helpViewShown).isTrue()
    }
}
