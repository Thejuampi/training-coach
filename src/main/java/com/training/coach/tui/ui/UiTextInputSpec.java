package com.training.coach.tui.ui;

import java.util.function.Consumer;
import java.util.function.Supplier;

public record UiTextInputSpec(String label, Supplier<String> getter, Consumer<String> setter) implements UiComponentSpec {}

