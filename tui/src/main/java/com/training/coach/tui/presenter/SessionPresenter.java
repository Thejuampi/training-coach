package com.training.coach.tui.presenter;

import com.training.coach.tui.TuiState;
import com.training.coach.tui.application.port.TrainingCoachGateway;
import com.training.coach.tui.ui.TuiNavigator;
import com.training.coach.tui.ui.UiButtonSpec;
import com.training.coach.tui.ui.UiLabelSpec;
import com.training.coach.tui.ui.UiRadioSpec;
import com.training.coach.tui.ui.UiSpacerSpec;
import com.training.coach.tui.ui.UiSpec;
import com.training.coach.tui.ui.UiTextInputSpec;
import com.training.coach.tui.dto.UserRole;
import java.util.List;

public class SessionPresenter {

    private final TuiState state;
    private final TuiNavigator navigator;
    private final TrainingCoachGateway gateway;

    public SessionPresenter(TuiState state, TuiNavigator navigator, TrainingCoachGateway gateway) {
        this.state = state;
        this.navigator = navigator;
        this.gateway = gateway;
    }

    public UiSpec build() {
        return new UiSpec(
                "Training Coach - Session",
                List.of(
                        new UiLabelSpec(messageOrDefault()),
                        new UiTextInputSpec(
                                "Base URL (REST API)", () -> nullToEmpty(state.baseUrl()), state::setBaseUrl),
                        new UiSpacerSpec(1),
                        new UiLabelSpec("Select Role:"),
                        new UiRadioSpec<>(
                                "Role",
                                List.of(UserRole.COACH, UserRole.ATHLETE, UserRole.ADMIN),
                                state::userRole,
                                state::setUserRole,
                                UserRole::name),
                        new UiSpacerSpec(1),
                        new UiTextInputSpec("Username", () -> nullToEmpty(state.username()), state::setUsername),
                        new UiTextInputSpec("Password", () -> nullToEmpty(state.password()), state::setPassword),
                        new UiTextInputSpec("User ID (optional)", () -> nullToEmpty(state.userId()), state::setUserId),
                        new UiTextInputSpec(
                                "Athlete ID (optional)", () -> nullToEmpty(state.athleteId()), state::setAthleteId),
                        new UiSpacerSpec(1),
                        new UiButtonSpec("Login & Continue", this::loginAndContinue)));
    }

    private void loginAndContinue() {
        try {
            if (state.username() == null || state.username().isBlank()) {
                state.setSessionMessage("Username is required.");
                navigator.show("session");
                return;
            }
            var tokens = gateway.login(state.username(), state.password() == null ? "" : state.password());
            state.setAccessToken(tokens.accessToken());
            state.setRefreshToken(tokens.refreshToken());
            state.setPassword(null);
            state.setSessionMessage("Login successful.");
            navigator.show("main");
        } catch (Exception ex) {
            state.setSessionMessage("Login failed: " + ex.getMessage());
            navigator.show("session");
        }
    }

    private String messageOrDefault() {
        String message = state.sessionMessage();
        return message == null || message.isBlank() ? "Enter your connection details." : message;
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
