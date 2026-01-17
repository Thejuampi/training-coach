package com.training.coach.tui.presenter;

import com.training.coach.athlete.domain.model.TrainingPlan;
import com.training.coach.athlete.domain.model.Workout;
import com.training.coach.tui.TuiState;
import com.training.coach.tui.ui.TuiNavigator;
import com.training.coach.tui.ui.UiButtonSpec;
import com.training.coach.tui.ui.UiLabelSpec;
import com.training.coach.tui.ui.UiSpec;
import com.training.coach.tui.ui.UiSpacerSpec;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PlanPreviewPresenter {

    private static final int MAX_WORKOUT_LINES = 12;

    private final TuiState state;
    private final TuiNavigator navigator;

    public PlanPreviewPresenter(TuiState state, TuiNavigator navigator) {
        this.state = state;
        this.navigator = navigator;
    }

    public UiSpec build() {
        List<com.training.coach.tui.ui.UiComponentSpec> components = new ArrayList<>();
        components.add(new UiLabelSpec("Training Plan Preview"));
        components.add(new UiSpacerSpec(1));

        TrainingPlan plan = state.lastGeneratedPlan();
        if (plan == null) {
            components.add(new UiLabelSpec("No plan generated yet."));
            components.add(new UiSpacerSpec(1));
            components.add(new UiButtonSpec("Back to Wizard", () -> navigator.show("plans:generate")));
            components.add(new UiButtonSpec("Back to Menu", () -> navigator.show("main")));
            return new UiSpec("Training Coach - Plan Preview", components);
        }

        components.add(new UiLabelSpec("Plan ID: " + plan.id()));
        components.add(new UiLabelSpec("Athlete ID: " + plan.athleteId()));
        components.add(new UiLabelSpec("Range: " + plan.startDate() + " to " + plan.endDate()));
        components.add(new UiLabelSpec("Workouts: " + plan.workouts().size()));
        components.add(new UiLabelSpec("Total Hours: " + plan.totalVolumeHours().value()));
        if (plan.notes() != null && !plan.notes().isBlank()) {
            components.add(new UiLabelSpec("Notes: " + plan.notes()));
        }
        components.add(new UiSpacerSpec(1));
        components.add(new UiLabelSpec("Workouts (first " + MAX_WORKOUT_LINES + "):"));

        List<Workout> workouts = new ArrayList<>(plan.workouts());
        workouts.sort(Comparator.comparing(Workout::date));
        int count = Math.min(MAX_WORKOUT_LINES, workouts.size());
        for (int i = 0; i < count; i++) {
            Workout workout = workouts.get(i);
            String line = workout.date()
                    + " | "
                    + workout.type()
                    + " | "
                    + workout.durationMinutes().value()
                    + " min | intervals: "
                    + workout.intervals().size();
            components.add(new UiLabelSpec(line));
        }
        if (workouts.size() > MAX_WORKOUT_LINES) {
            components.add(new UiLabelSpec("... and " + (workouts.size() - MAX_WORKOUT_LINES) + " more"));
        }

        components.add(new UiSpacerSpec(1));
        components.add(new UiButtonSpec("Back to Wizard", () -> navigator.show("plans:generate")));
        components.add(new UiButtonSpec("Back to Menu", () -> navigator.show("main")));

        return new UiSpec("Training Coach - Plan Preview", components);
    }
}
