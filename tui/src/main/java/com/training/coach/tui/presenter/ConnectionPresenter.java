package com.training.coach.tui.presenter;

import com.training.coach.tui.TuiState;
import com.training.coach.tui.ui.TuiNavigator;
import com.training.coach.tui.ui.UiButtonSpec;
import com.training.coach.tui.ui.UiLabelSpec;
import com.training.coach.tui.ui.UiSpacerSpec;
import com.training.coach.tui.ui.UiSpec;
import com.training.coach.tui.ui.UiTextInputSpec;
import java.util.List;

public class ConnectionPresenter {

    private final TuiState state;
    private final TuiNavigator navigator;

    public ConnectionPresenter(TuiState state, TuiNavigator navigator) {
        this.state = state;
        this.navigator = navigator;
    }

    public UiSpec build() {
        return new UiSpec(
                "Training Coach - Connection",
                List.of(
                        new UiLabelSpec("Base URL (REST API):"),
                        new UiTextInputSpec("Base URL", state::baseUrl, state::setBaseUrl),
                        new UiSpacerSpec(1),
                        new UiButtonSpec("Continue", () -> navigator.show("session"))));
    }
}
