package com.training.coach.shared.domain.unit;

/**
 * Immutable record representing a range of power values in watts.
 */
public record WattsRange(Watts lower, Watts upper) {
    public WattsRange {
        if (lower == null) {
            throw new IllegalArgumentException("Lower bound cannot be null");
        }
        if (upper == null) {
            throw new IllegalArgumentException("Upper bound cannot be null");
        }
        if (upper.value() < lower.value()) {
            throw new IllegalArgumentException("Upper bound must be >= lower bound");
        }
    }

    public static WattsRange of(double lowerWatts, double upperWatts) {
        return new WattsRange(Watts.of(lowerWatts), Watts.of(upperWatts));
    }

    public static WattsRange centerOn(Watts center, double percentWidth) {
        double halfWidth = center.value() * percentWidth / 100.0;
        return new WattsRange(Watts.of(center.value() - halfWidth), Watts.of(center.value() + halfWidth));
    }

    public boolean contains(Watts value) {
        return value.value() >= lower.value() && value.value() <= upper.value();
    }
}
