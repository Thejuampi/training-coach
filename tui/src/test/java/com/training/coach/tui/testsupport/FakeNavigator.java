package com.training.coach.tui.testsupport;

import com.training.coach.tui.ui.TuiNavigator;
import com.training.coach.tui.ui.TuiRenderer;

public class FakeNavigator extends TuiNavigator {

    private String lastRoute;

    public FakeNavigator() {
        super(new TuiRenderer(null));
    }

    @Override
    public void show(String route) {
        this.lastRoute = route;
    }

    public String lastRoute() {
        return lastRoute;
    }
}
