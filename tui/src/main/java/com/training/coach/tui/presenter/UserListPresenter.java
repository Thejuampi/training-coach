package com.training.coach.tui.presenter;

import com.training.coach.tui.TuiState;
import com.training.coach.tui.application.port.TrainingCoachGateway;
import com.training.coach.tui.ui.TuiNavigator;
import com.training.coach.tui.ui.UiButtonSpec;
import com.training.coach.tui.ui.UiLabelSpec;
import com.training.coach.tui.ui.UiSpacerSpec;
import com.training.coach.tui.ui.UiSpec;
import com.training.coach.tui.dto.SystemUser;
import java.util.ArrayList;
import java.util.List;

public class UserListPresenter {

    private final TuiState state;
    private final TuiNavigator navigator;
    private final TrainingCoachGateway gateway;

    public UserListPresenter(TuiState state, TuiNavigator navigator, TrainingCoachGateway gateway) {
        this.state = state;
        this.navigator = navigator;
        this.gateway = gateway;
    }

    public UiSpec build() {
        List<com.training.coach.tui.ui.UiComponentSpec> components = new ArrayList<>();
        components.add(new UiLabelSpec("Users"));
        components.add(new UiSpacerSpec(1));
        try {
            SystemUser[] users = gateway.listUsers();
            if (users.length == 0) {
                components.add(new UiLabelSpec("No users found."));
            } else {
                for (SystemUser user : users) {
                    components.add(new UiLabelSpec(formatUser(user)));
                }
            }
        } catch (Exception ex) {
            components.add(new UiLabelSpec("Failed to load users: " + ex.getMessage()));
        }

        components.add(new UiSpacerSpec(1));
        components.add(new UiButtonSpec("Back", () -> navigator.show("main")));
        return new UiSpec("Training Coach - Users", components);
    }

    private String formatUser(SystemUser user) {
        return user.name() + " | " + user.role() + " | " + user.id();
    }
}
