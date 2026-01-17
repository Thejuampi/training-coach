package com.training.coach.tui.presenter;

import com.training.coach.athlete.domain.model.Athlete;
import com.training.coach.shared.domain.unit.Hours;
import com.training.coach.tui.TuiState;
import com.training.coach.tui.application.port.TrainingCoachGateway;
import com.training.coach.tui.ui.TuiNavigator;
import com.training.coach.tui.ui.UiButtonSpec;
import com.training.coach.tui.ui.UiLabelSpec;
import com.training.coach.tui.ui.UiSpacerSpec;
import com.training.coach.tui.ui.UiSpec;
import com.training.coach.tui.ui.UiTextInputSpec;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class PlanGenerateWizardPresenter {

    private final TuiState state;
    private final TuiNavigator navigator;
    private final TrainingCoachGateway gateway;

    public PlanGenerateWizardPresenter(TuiState state, TuiNavigator navigator, TrainingCoachGateway gateway) {
        this.state = state;
        this.navigator = navigator;
        this.gateway = gateway;
    }

    public UiSpec build() {
        ensureDefaults();

        List<com.training.coach.tui.ui.UiComponentSpec> components = new ArrayList<>();
        components.add(new UiLabelSpec("Generate Training Plan"));
        components.add(new UiSpacerSpec(1));

        if (state.planError() != null && !state.planError().isBlank()) {
            components.add(new UiLabelSpec("Error: " + state.planError()));
            components.add(new UiSpacerSpec(1));
        }

        components.add(new UiTextInputSpec("Athlete ID", () -> nullToEmpty(state.athleteId()), state::setAthleteId));
        components.add(new UiTextInputSpec("Phase", () -> nullToEmpty(state.planPhase()), state::setPlanPhase));
        components.add(new UiTextInputSpec(
                "Start Date (YYYY-MM-DD)", () -> nullToEmpty(state.planStartDate()), state::setPlanStartDate));
        components.add(new UiTextInputSpec(
                "Target Weekly Hours",
                () -> nullToEmpty(state.planTargetWeeklyHours()),
                state::setPlanTargetWeeklyHours));
        components.add(new UiSpacerSpec(1));

        components.add(new UiButtonSpec("Generate", this::generatePlan));
        if (state.lastGeneratedPlan() != null) {
            components.add(new UiButtonSpec("Preview Last Plan", () -> navigator.show("plans:preview")));
        }
        components.add(new UiButtonSpec("Back to Menu", () -> navigator.show("main")));

        return new UiSpec("Training Coach - Plan Generate", components);
    }

    private void generatePlan() {
        String athleteId = state.athleteId();
        if (athleteId == null || athleteId.isBlank()) {
            state.setPlanError("Athlete ID is required");
            return;
        }

        LocalDate startDate;
        try {
            startDate = LocalDate.parse(nullToEmpty(state.planStartDate()));
        } catch (DateTimeParseException ex) {
            state.setPlanError("Start date must be YYYY-MM-DD");
            return;
        }

        double weeklyHours;
        try {
            weeklyHours = Double.parseDouble(nullToEmpty(state.planTargetWeeklyHours()));
        } catch (NumberFormatException ex) {
            state.setPlanError("Target weekly hours must be a number");
            return;
        }
        if (weeklyHours < 0) {
            state.setPlanError("Target weekly hours must be >= 0");
            return;
        }

        String phase = nullToEmpty(state.planPhase());
        if (phase.isBlank()) {
            state.setPlanError("Phase is required");
            return;
        }

        try {
            Athlete athlete = gateway.getAthlete(athleteId);
            state.setLastGeneratedPlan(gateway.generateTrainingPlan(athlete, phase, startDate, Hours.of(weeklyHours)));
            state.setPlanError(null);
            navigator.show("plans:preview");
        } catch (RuntimeException ex) {
            state.setPlanError(ex.getMessage() == null ? "Failed to generate plan" : ex.getMessage());
        }
    }

    private void ensureDefaults() {
        if (state.planPhase() == null) {
            state.setPlanPhase("Base");
        }
        if (state.planStartDate() == null) {
            state.setPlanStartDate(LocalDate.now().toString());
        }
        if (state.planTargetWeeklyHours() == null) {
            state.setPlanTargetWeeklyHours("6");
        }
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
