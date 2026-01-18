package com.training.coach.tui.dto;

import com.training.coach.shared.domain.unit.Centimeters;
import com.training.coach.shared.domain.unit.Kilograms;

/**
 * Immutable value object representing athlete's profile information.
 */
public record AthleteProfile(String gender, int age, Kilograms weightKg, Centimeters heightCm, String level) {}