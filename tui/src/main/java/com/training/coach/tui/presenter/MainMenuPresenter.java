package com.training.coach.tui.presenter;

import com.training.coach.tui.TuiState;
import com.training.coach.tui.ui.TuiNavigator;
import com.training.coach.tui.ui.UiButtonSpec;
import com.training.coach.tui.ui.UiLabelSpec;
import com.training.coach.tui.ui.UiSpacerSpec;
import com.training.coach.tui.ui.UiSpec;
import com.training.coach.tui.dto.UserRole;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MainMenuPresenter {

    private final TuiState state;
    private final TuiNavigator navigator;

    public MainMenuPresenter(TuiState state, TuiNavigator navigator) {
        this.state = state;
        this.navigator = navigator;
    }

    public UiSpec build() {
        List<UiButtonSpec> buttons = new ArrayList<>();
        for (var entry : menuForRole(state.userRole()).entrySet()) {
            String route = resolveRoute(entry.getValue());
            buttons.add(new UiButtonSpec(entry.getKey(), () -> navigator.show(route)));
        }

        List<com.training.coach.tui.ui.UiComponentSpec> components = new ArrayList<>();
        components.add(new UiLabelSpec("Role: " + state.userRole()));
        components.add(new UiLabelSpec("Base URL: " + state.baseUrl()));
        components.add(new UiSpacerSpec(1));
        components.addAll(buttons);
        components.add(new UiSpacerSpec(1));
        components.add(new UiButtonSpec("Change Session", () -> navigator.show("session")));

        return new UiSpec("Training Coach - Main Menu", components);
    }

    private Map<String, String> menuForRole(UserRole role) {
        Map<String, String> items = new LinkedHashMap<>();
        items.put("Athletes", "main");
        items.put("Wellness & Readiness", "main");
        items.put("Activities", "main");
        items.put("Plans (Generate/Review)", "plans:generate");
        items.put("Compliance & Progress", "main");
        items.put("Adjust Plan", "main");
        items.put("Notes / Communication", "main");
        items.put("Testing & Zones (LT1/LT2)", "main");
        items.put("Availability & Calendar", "main");
        items.put("Events / Races", "main");
        items.put("Notifications", "main");
        items.put("Reports & Exports", "main");
        items.put("Safety & Guardrails", "main");

        if (role == UserRole.ADMIN) {
            items.put("Users & Roles", "users");
            items.put("Integrations", "main");
            items.put("Data Privacy", "main");
        } else if (role == UserRole.COACH) {
            items.put("Integrations (Status)", "main");
        }

        return items;
    }

    private String resolveRoute(String value) {
        if (value != null && value.contains(":")) {
            return value;
        }
        return "main";
    }
}
