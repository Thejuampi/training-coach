package com.training.coach.tui.ui;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public record UiRadioSpec<T>(
        String label, List<T> options, Supplier<T> getter, Consumer<T> setter, java.util.function.Function<T, String> display)
        implements UiComponentSpec {}

