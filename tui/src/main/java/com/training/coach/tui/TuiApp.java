package com.training.coach.tui;

import com.googlecode.lanterna.gui2.MultiWindowTextGUI;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.training.coach.tui.application.port.TrainingCoachGateway;
import com.training.coach.tui.infrastructure.adapter.RestTrainingCoachGateway;
import com.training.coach.tui.presenter.ConnectionPresenter;
import com.training.coach.tui.presenter.MainMenuPresenter;
import com.training.coach.tui.presenter.PlanGenerateWizardPresenter;
import com.training.coach.tui.presenter.PlanPreviewPresenter;
import com.training.coach.tui.presenter.SessionPresenter;
import com.training.coach.tui.presenter.UserAdminPresenter;
import com.training.coach.tui.ui.TuiNavigator;
import com.training.coach.tui.ui.TuiRenderer;
import java.io.IOException;

/**
 * Declarative TUI entrypoint. All UI composition is defined in UiSpec objects and rendered by TuiRenderer.
 */
public class TuiApp {

    public static void main(String[] args) throws IOException {
        String baseUrl = System.getenv("BACKEND_BASE_URL");
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = args.length > 0 ? args[0] : "http://localhost:8080";
        }
        DefaultTerminalFactory terminalFactory = new DefaultTerminalFactory();
        Screen screen = terminalFactory.createScreen();
        screen.startScreen();

        MultiWindowTextGUI gui = new MultiWindowTextGUI(screen);
        TuiState state = new TuiState();
        state.setBaseUrl(baseUrl);
        TuiRenderer renderer = new TuiRenderer(gui);
        TuiNavigator navigator = new TuiNavigator(renderer);
        TrainingCoachGateway gateway = new RestTrainingCoachGateway(state);

        ConnectionPresenter connectionPresenter = new ConnectionPresenter(state, navigator);
        SessionPresenter sessionPresenter = new SessionPresenter(state, navigator, gateway);
        MainMenuPresenter mainMenuPresenter = new MainMenuPresenter(state, navigator);
        PlanGenerateWizardPresenter planGenerateWizardPresenter =
                new PlanGenerateWizardPresenter(state, navigator, gateway);
        PlanPreviewPresenter planPreviewPresenter = new PlanPreviewPresenter(state, navigator);
        UserAdminPresenter userAdminPresenter = new UserAdminPresenter(state, navigator, gateway);

        navigator.register("connection", connectionPresenter::build);
        navigator.register("session", sessionPresenter::build);
        navigator.register("main", mainMenuPresenter::build);
        navigator.register("plans:generate", planGenerateWizardPresenter::build);
        navigator.register("plans:preview", planPreviewPresenter::build);
        navigator.register("users", userAdminPresenter::build);

        navigator.show("session");
        screen.stopScreen();
    }
}
