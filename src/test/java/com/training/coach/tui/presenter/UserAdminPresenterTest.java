package com.training.coach.tui.presenter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.training.coach.tui.TuiState;
import com.training.coach.tui.application.port.TrainingCoachGateway;
import com.training.coach.tui.ui.TuiNavigator;
import com.training.coach.tui.ui.UiComponentSpec;
import com.training.coach.tui.ui.UiRadioSpec;
import com.training.coach.tui.ui.UiSpec;
import com.training.coach.tui.ui.UiTextInputSpec;
import java.util.List;
import org.junit.jupiter.api.Test;

class UserAdminPresenterTest {

    @Test
    void buildIncludesRoleRadioAndConfirmationInput() {
        TuiState state = new TuiState();
        TrainingCoachGateway gateway = mock(TrainingCoachGateway.class);
        TuiNavigator navigator = mock(TuiNavigator.class);
        UserAdminPresenter presenter = new UserAdminPresenter(state, navigator, gateway);

        UiSpec spec = presenter.build();
        List<UiComponentSpec> components = spec.components();

        boolean hasRoleRadio = components.stream().anyMatch(c -> c instanceof UiRadioSpec<?>);
        boolean hasConfirmationInput = components.stream()
                .filter(c -> c instanceof UiTextInputSpec)
                .map(c -> (UiTextInputSpec) c)
                .anyMatch(input -> input.label().toLowerCase().contains("confirm"));

        assertThat(hasRoleRadio).isTrue();
        assertThat(hasConfirmationInput).isTrue();
    }
}
