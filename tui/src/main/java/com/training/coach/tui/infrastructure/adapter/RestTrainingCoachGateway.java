package com.training.coach.tui.infrastructure.adapter;

import com.training.coach.common.AuthTokens;
import com.training.coach.shared.domain.unit.Hours;
import com.training.coach.tui.TuiState;
import com.training.coach.tui.application.port.TrainingCoachGateway;
import com.training.coach.tui.dto.Athlete;
import com.training.coach.tui.dto.SystemUser;
import com.training.coach.tui.dto.TrainingPlan;
import com.training.coach.tui.dto.UserPreferences;
import java.time.LocalDate;
import java.util.Objects;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

public class RestTrainingCoachGateway implements TrainingCoachGateway {

    private final TuiState state;

    public RestTrainingCoachGateway(TuiState state) {
        this.state = state;
    }

    @Override
    public AuthTokens login(String username, String password) {
        AuthTokens tokens = baseClient()
                .post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new LoginRequest(username, password))
                .retrieve()
                .bodyToMono(AuthTokens.class)
                .block();
        return Objects.requireNonNull(tokens, "Auth response was null");
    }

    @Override
    public Athlete getAthlete(String athleteId) {
        Athlete athlete = authorizedClient()
                .get()
                .uri("/api/athletes/{id}", athleteId)
                .retrieve()
                .bodyToMono(Athlete.class)
                .block();
        return Objects.requireNonNull(athlete, "Athlete response was null");
    }

    @Override
    public TrainingPlan generateTrainingPlan(
            Athlete athlete, String phase, LocalDate startDate, Hours targetWeeklyHours) {
        TrainingPlan plan = authorizedClient()
                .post()
                .uri("/api/training-plans/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new GeneratePlanRequest(athlete, phase, startDate, targetWeeklyHours))
                .retrieve()
                .bodyToMono(TrainingPlan.class)
                .block();
        return Objects.requireNonNull(plan, "Training plan response was null");
    }

    @Override
    public SystemUser[] listUsers() {
        SystemUser[] users = authorizedClient()
                .get()
                .uri("/api/users")
                .retrieve()
                .bodyToMono(SystemUser[].class)
                .block();
        return Objects.requireNonNull(users, "Users response was null");
    }

    @Override
    public SystemUser createUser(String name, String role, String username, String password) {
        String normalizedRole = role == null ? "COACH" : role.trim().toUpperCase();
        SystemUser created = authorizedClient()
                .post()
                .uri("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new CreateUserRequest(
                        name, normalizedRole, UserPreferences.metricDefaults(), username, password))
                .retrieve()
                .bodyToMono(SystemUser.class)
                .block();
        return Objects.requireNonNull(created, "Create user response was null");
    }

    @Override
    public void setPassword(String userId, String newPassword) {
        authorizedClient()
                .post()
                .uri("/api/users/{id}/password", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new PasswordRequest(newPassword))
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    @Override
    public String rotatePassword(String userId) {
        RotatePasswordResponse response = authorizedClient()
                .post()
                .uri("/api/users/{id}/password/rotate", userId)
                .retrieve()
                .bodyToMono(RotatePasswordResponse.class)
                .block();
        return Objects.requireNonNull(response, "Rotate password response was null")
                .password();
    }

    @Override
    public void disableUser(String userId) {
        authorizedClient()
                .post()
                .uri("/api/users/{id}/disable", userId)
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    @Override
    public void enableUser(String userId) {
        authorizedClient()
                .post()
                .uri("/api/users/{id}/enable", userId)
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    private WebClient baseClient() {
        return WebClient.builder()
                .baseUrl(state.baseUrl())
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    private WebClient authorizedClient() {
        WebClient.Builder builder = WebClient.builder()
                .baseUrl(state.baseUrl())
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        String accessToken = state.accessToken();
        if (accessToken != null && !accessToken.isBlank()) {
            builder.defaultHeaders(headers -> headers.setBearerAuth(accessToken));
        }
        return builder.build();
    }

    private record GeneratePlanRequest(Athlete athlete, String phase, LocalDate startDate, Hours targetWeeklyHours) {}

    private record CreateUserRequest(
            String name, String role, UserPreferences preferences, String username, String password) {}

    private record LoginRequest(String username, String password) {}

    private record PasswordRequest(String newPassword) {}

    private record RotatePasswordResponse(String password) {}
}
