package com.training.coach.tui.testsupport;

import com.training.coach.tui.ui.UiButtonSpec;
import com.training.coach.tui.ui.UiComponentSpec;
import com.training.coach.tui.ui.UiLabelSpec;
import com.training.coach.tui.ui.UiSpec;
import java.util.List;
import java.util.stream.Collectors;

public final class UiSpecAsserts {

    private UiSpecAsserts() {}

    public static List<String> labels(UiSpec spec) {
        return spec.components().stream()
                .filter(component -> component instanceof UiLabelSpec)
                .map(component -> ((UiLabelSpec) component).text())
                .collect(Collectors.toList());
    }

    public static List<String> buttons(UiSpec spec) {
        return spec.components().stream()
                .filter(component -> component instanceof UiButtonSpec)
                .map(component -> ((UiButtonSpec) component).label())
                .collect(Collectors.toList());
    }

    public static long countType(UiSpec spec, Class<?> type) {
        return spec.components().stream().filter(type::isInstance).count();
    }
}
