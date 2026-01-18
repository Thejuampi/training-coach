package com.training.coach.tui.acceptance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.training.coach.tui.TuiState;
import com.training.coach.tui.application.port.TrainingCoachGateway;
import com.training.coach.tui.presenter.SessionPresenter;
import com.training.coach.tui.ui.TuiNavigator;
import com.training.coach.tui.ui.UiButtonSpec;
import com.training.coach.tui.ui.UiComponentSpec;
import com.training.coach.tui.ui.UiSpec;
import com.training.coach.tui.ui.UiTextInputSpec;
import java.util.List;
import org.junit.jupiter.api.Test;

class UC0_LoginFirstTuiTest {

    @Test
    void whenNoSessionExists_initialTuiScreenIsLogin() {
        // Given no session exists (no access token)
        TuiState state = new TuiState();
        state.setAccessToken(null); // explicitly no session
        TrainingCoachGateway gateway = mock(TrainingCoachGateway.class);
        TuiNavigator navigator = mock(TuiNavigator.class);

        // When building the initial screen
        SessionPresenter presenter = new SessionPresenter(state, navigator, gateway);
        UiSpec spec = presenter.build();

        // Then the screen is the Login screen
        assertThat(spec.title()).isEqualTo("Training Coach - Session");

        List<UiComponentSpec> components = spec.components();
        boolean hasUsernameInput = components.stream()
                .filter(c -> c instanceof UiTextInputSpec)
                .map(c -> (UiTextInputSpec) c)
                .anyMatch(input -> "Username".equals(input.label()));
        boolean hasPasswordInput = components.stream()
                .filter(c -> c instanceof UiTextInputSpec)
                .map(c -> (UiTextInputSpec) c)
                .anyMatch(input -> "Password".equals(input.label()));
        boolean hasLoginButton = components.stream()
                .filter(c -> c instanceof UiButtonSpec)
                .map(c -> (UiButtonSpec) c)
                .anyMatch(button -> "Login & Continue".equals(button.label()));

        assertThat(hasUsernameInput).isTrue();
        assertThat(hasPasswordInput).isTrue();
        assertThat(hasLoginButton).isTrue();
    }
}