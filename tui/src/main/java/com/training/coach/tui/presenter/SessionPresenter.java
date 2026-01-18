package com.training.coach.tui.presenter;

import com.training.coach.tui.TuiState;
import com.training.coach.tui.application.port.TrainingCoachGateway;
import com.training.coach.tui.ui.TuiNavigator;
import com.training.coach.tui.ui.UiButtonSpec;
import com.training.coach.tui.ui.UiLabelSpec;
import com.training.coach.tui.ui.UiSpacerSpec;
import com.training.coach.tui.ui.UiSpec;
import com.training.coach.tui.dto.UserRole;
import com.training.coach.tui.ui.UiTextInputSpec;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
                        new UiTextInputSpec("Username", () -> nullToEmpty(state.username()), state::setUsername),
                        new UiTextInputSpec("Password", () -> nullToEmpty(state.password()), state::setPassword),
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
            String claimMessage = applyJwtClaims(tokens.accessToken());
            state.setPassword(null);
            if (claimMessage == null || claimMessage.isBlank()) {
                state.setSessionMessage("Login successful.");
            } else {
                state.setSessionMessage(claimMessage);
                navigator.show("session");
                return;
            }
            navigator.show("main");
        } catch (Exception ex) {
            state.setSessionMessage("Login failed: " + ex.getMessage());
            navigator.show("session");
        }
    }

    private String applyJwtClaims(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            return "Login succeeded, but token was empty.";
        }
        String[] parts = accessToken.split("\\.");
        if (parts.length < 2) {
            return "Login succeeded, but token payload was missing.";
        }
        String payload;
        try {
            payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException ex) {
            return "Login succeeded, but token could not be decoded.";
        }
        String subject = extractStringClaim(payload, "sub");
        if (subject == null || subject.isBlank()) {
            return "Login succeeded, but token payload was missing subject.";
        }
        state.setSubject(subject);
        state.setUserId(subject);
        List<UserRole> roles = extractRoles(payload);
        if (roles.isEmpty()) {
            return "Login succeeded, but token payload was missing roles.";
        }
        state.setUserRoles(roles);
        state.setUserRole(roles.get(0));
        return null;
    }

    private String extractStringClaim(String payload, String claimName) {
        Pattern pattern = Pattern.compile(
                "\"" + Pattern.quote(claimName) + "\"\\s*:\\s*\"(.*?)\"",
                Pattern.DOTALL);
        Matcher matcher = pattern.matcher(payload);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private List<UserRole> extractRoles(String payload) {
        Pattern rolesPattern = Pattern.compile("\"roles\"\\s*:\\s*\\[(.*?)\\]", Pattern.DOTALL);
        Matcher matcher = rolesPattern.matcher(payload);
        if (!matcher.find()) {
            return new ArrayList<>();
        }
        String listContent = matcher.group(1);
        Pattern rolePattern = Pattern.compile("\"(.*?)\"");
        Matcher roleMatcher = rolePattern.matcher(listContent);
        List<UserRole> roles = new ArrayList<>();
        while (roleMatcher.find()) {
            String roleValue = roleMatcher.group(1);
            try {
                roles.add(UserRole.valueOf(roleValue.trim().toUpperCase()));
            } catch (IllegalArgumentException ex) {
                // ignore unknown roles
            }
        }
        return roles;
    }

    private String messageOrDefault() {
        String message = state.sessionMessage();
        return message == null || message.isBlank() ? "Enter your connection details." : message;
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
