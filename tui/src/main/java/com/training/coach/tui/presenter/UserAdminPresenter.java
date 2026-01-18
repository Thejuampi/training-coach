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
import com.training.coach.tui.dto.SystemUser;
import com.training.coach.tui.dto.UserRole;
import java.util.ArrayList;
import java.util.List;

public class UserAdminPresenter {

    private final TuiState state;
    private final TuiNavigator navigator;
    private final TrainingCoachGateway gateway;

    public UserAdminPresenter(TuiState state, TuiNavigator navigator, TrainingCoachGateway gateway) {
        this.state = state;
        this.navigator = navigator;
        this.gateway = gateway;
    }

    public UiSpec build() {
        ensureDefaults();
        List<com.training.coach.tui.ui.UiComponentSpec> components = new ArrayList<>();
        components.add(new UiLabelSpec("Users & Roles (Admin)"));
        components.add(new UiSpacerSpec(1));

        if (state.adminMessage() != null && !state.adminMessage().isBlank()) {
            components.add(new UiLabelSpec(state.adminMessage()));
            components.add(new UiSpacerSpec(1));
        }

        components.add(new UiLabelSpec("Existing Users:"));
        try {
            SystemUser[] users = gateway.listUsers();
            if (users.length == 0) {
                components.add(new UiLabelSpec("No users found."));
            } else {
                for (SystemUser user : users) {
                    components.add(new UiLabelSpec(formatUser(user)));
                }
            }
        } catch (RuntimeException ex) {
            components.add(new UiLabelSpec("Failed to load users: " + ex.getMessage()));
        }

        components.add(new UiSpacerSpec(1));
        components.add(new UiLabelSpec("Create User"));
        components.add(
                new UiTextInputSpec("Name", () -> nullToEmpty(state.adminNewUserName()), state::setAdminNewUserName));
        components.add(new UiRadioSpec<>(
                "Role",
                List.of(UserRole.COACH, UserRole.ATHLETE, UserRole.ADMIN),
                () -> parseRole(state.adminNewUserRole()),
                role -> state.setAdminNewUserRole(role.name()),
                UserRole::name));
        components.add(new UiTextInputSpec(
                "Username", () -> nullToEmpty(state.adminNewUserUsername()), state::setAdminNewUserUsername));
        components.add(new UiTextInputSpec(
                "Password", () -> nullToEmpty(state.adminNewUserPassword()), state::setAdminNewUserPassword));
        components.add(new UiButtonSpec("Create User", this::createUser));

        components.add(new UiSpacerSpec(1));
        components.add(new UiLabelSpec("Manage Credentials"));
        components.add(new UiTextInputSpec(
                "Target User ID", () -> nullToEmpty(state.adminTargetUserId()), state::setAdminTargetUserId));
        components.add(new UiTextInputSpec(
                "New Password", () -> nullToEmpty(state.adminNewPassword()), state::setAdminNewPassword));
        components.add(new UiTextInputSpec(
                "Confirm (type YES)", () -> nullToEmpty(state.adminConfirmAction()), state::setAdminConfirmAction));
        components.add(new UiButtonSpec("Set Password", this::setPassword));
        components.add(new UiButtonSpec("Rotate Password", this::rotatePassword));
        components.add(new UiButtonSpec("Disable User", this::disableUser));
        components.add(new UiButtonSpec("Enable User", this::enableUser));

        components.add(new UiSpacerSpec(1));
        components.add(new UiButtonSpec("Back", () -> navigator.show("main")));

        return new UiSpec("Training Coach - Users", components);
    }

    private void createUser() {
        try {
            SystemUser user = gateway.createUser(
                    nullToEmpty(state.adminNewUserName()),
                    nullToEmpty(state.adminNewUserRole()),
                    nullToEmpty(state.adminNewUserUsername()),
                    nullToEmpty(state.adminNewUserPassword()));
            state.setAdminMessage("Created user: " + user.id());
        } catch (RuntimeException ex) {
            state.setAdminMessage("Create user failed: " + ex.getMessage());
        }
        navigator.show("users");
    }

    private void setPassword() {
        if (!confirmed()) {
            state.setAdminMessage("Confirmation required (type YES).");
            navigator.show("users");
            return;
        }
        try {
            gateway.setPassword(nullToEmpty(state.adminTargetUserId()), nullToEmpty(state.adminNewPassword()));
            state.setAdminMessage("Password updated for user: " + state.adminTargetUserId());
        } catch (RuntimeException ex) {
            state.setAdminMessage("Set password failed: " + ex.getMessage());
        }
        navigator.show("users");
    }

    private void rotatePassword() {
        if (!confirmed()) {
            state.setAdminMessage("Confirmation required (type YES).");
            navigator.show("users");
            return;
        }
        try {
            String password = gateway.rotatePassword(nullToEmpty(state.adminTargetUserId()));
            state.setAdminMessage("Rotated password: " + password);
        } catch (RuntimeException ex) {
            state.setAdminMessage("Rotate password failed: " + ex.getMessage());
        }
        navigator.show("users");
    }

    private void disableUser() {
        if (!confirmed()) {
            state.setAdminMessage("Confirmation required (type YES).");
            navigator.show("users");
            return;
        }
        try {
            gateway.disableUser(nullToEmpty(state.adminTargetUserId()));
            state.setAdminMessage("Disabled user: " + state.adminTargetUserId());
        } catch (RuntimeException ex) {
            state.setAdminMessage("Disable failed: " + ex.getMessage());
        }
        navigator.show("users");
    }

    private void enableUser() {
        if (!confirmed()) {
            state.setAdminMessage("Confirmation required (type YES).");
            navigator.show("users");
            return;
        }
        try {
            gateway.enableUser(nullToEmpty(state.adminTargetUserId()));
            state.setAdminMessage("Enabled user: " + state.adminTargetUserId());
        } catch (RuntimeException ex) {
            state.setAdminMessage("Enable failed: " + ex.getMessage());
        }
        navigator.show("users");
    }

    private void ensureDefaults() {
        if (state.adminNewUserRole() == null || state.adminNewUserRole().isBlank()) {
            state.setAdminNewUserRole("COACH");
        }
    }

    private boolean confirmed() {
        return "YES".equalsIgnoreCase(nullToEmpty(state.adminConfirmAction()).trim());
    }

    private String formatUser(SystemUser user) {
        return user.name() + " | " + user.role() + " | " + user.id();
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private UserRole parseRole(String value) {
        if (value == null || value.isBlank()) {
            return UserRole.COACH;
        }
        try {
            return UserRole.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return UserRole.COACH;
        }
    }
}
