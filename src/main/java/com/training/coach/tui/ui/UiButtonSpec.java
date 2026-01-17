package com.training.coach.tui.ui;

public record UiButtonSpec(String label, Runnable onClick) implements UiComponentSpec {}

