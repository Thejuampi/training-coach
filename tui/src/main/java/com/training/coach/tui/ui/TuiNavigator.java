package com.training.coach.tui.ui;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class TuiNavigator {

    private final TuiRenderer renderer;
    private final Map<String, Supplier<UiSpec>> routes = new HashMap<>();

    public TuiNavigator(TuiRenderer renderer) {
        this.renderer = renderer;
    }

    public void register(String route, Supplier<UiSpec> specSupplier) {
        routes.put(route, specSupplier);
    }

    public void show(String route) {
        Supplier<UiSpec> supplier = routes.get(route);
        if (supplier == null) {
            throw new IllegalArgumentException("No route registered for " + route);
        }
        renderer.render(supplier.get());
    }
}
