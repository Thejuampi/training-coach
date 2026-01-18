package com.training.coach.tui.dto;

import com.training.coach.shared.domain.unit.Hours;
import java.time.DayOfWeek;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Immutable value object representing athlete's training preferences.
 */
public record TrainingPreferences(Set<DayOfWeek> availableDays, Hours targetWeeklyVolumeHours, String currentPhase) {
    public TrainingPreferences {
        if (availableDays == null) {
            availableDays = Collections.emptySet();
        } else {
            availableDays = Collections.unmodifiableSet(new HashSet<>(availableDays));
        }
    }
}