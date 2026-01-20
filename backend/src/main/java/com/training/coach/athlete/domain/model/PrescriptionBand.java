package com.training.coach.athlete.domain.model;

import com.training.coach.analysis.application.service.SeilerZone;
import com.training.coach.shared.domain.unit.WattsRange;

/**
 * Immutable record representing a prescription band with target range, method, and confidence.
 */
public record PrescriptionBand(
        SeilerZone zone,
        WattsRange targetRange,
        String method,
        double confidence
) {
    public PrescriptionBand {
        if (zone == null) {
            throw new IllegalArgumentException("Zone cannot be null");
        }
        if (targetRange == null) {
            throw new IllegalArgumentException("Target range cannot be null");
        }
        if (method == null || method.isBlank()) {
            throw new IllegalArgumentException("Method cannot be null or blank");
        }
        if (confidence < 0 || confidence > 100) {
            throw new IllegalArgumentException("Confidence must be 0-100");
        }
    }
}
