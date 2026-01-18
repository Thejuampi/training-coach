package com.training.coach.analysis.application.service;

import com.training.coach.shared.domain.unit.Minutes;

public record SeilerZoneDistribution(Minutes z1Minutes, Minutes z2Minutes, Minutes z3Minutes) {
    public SeilerZoneDistribution {
        if (z1Minutes == null || z2Minutes == null || z3Minutes == null) {
            throw new IllegalArgumentException("Zone minutes cannot be null");
        }
        if (z1Minutes.value() < 0 || z2Minutes.value() < 0 || z3Minutes.value() < 0) {
            throw new IllegalArgumentException("Zone minutes must be non-negative");
        }
    }

    public Minutes totalMinutes() {
        return Minutes.of(z1Minutes.value() + z2Minutes.value() + z3Minutes.value());
    }

    public double percentZ1() {
        int total = totalMinutes().value();
        return total == 0 ? 0.0 : (double) z1Minutes.value() / total * 100.0;
    }

    public double percentZ2() {
        int total = totalMinutes().value();
        return total == 0 ? 0.0 : (double) z2Minutes.value() / total * 100.0;
    }

    public double percentZ3() {
        int total = totalMinutes().value();
        return total == 0 ? 0.0 : (double) z3Minutes.value() / total * 100.0;
    }
}
