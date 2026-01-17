package com.training.coach.analysis.application.service;

import com.training.coach.shared.domain.unit.Minutes;
import com.training.coach.shared.domain.unit.Watts;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class SeilerIntensityClassificationService {

    public record ClassificationResult(
            String athleteId,
            SeilerZoneDistribution distribution,
            String methodUsed,
            Watts lt1Watts,
            Watts lt2Watts,
            double confidence) {}

    public ClassificationResult classifyFromExplicitTimeInZones(
            String athleteId, Minutes z1, Minutes z2, Minutes z3, Optional<SeilerThresholds> thresholds) {
        SeilerZoneDistribution distribution = new SeilerZoneDistribution(z1, z2, z3);
        return new ClassificationResult(
                athleteId,
                distribution,
                "manual_time_in_zone",
                thresholds.map(SeilerThresholds::lt1Watts).orElse(null),
                thresholds.map(SeilerThresholds::lt2Watts).orElse(null),
                thresholds.map(SeilerThresholds::confidence).orElse(0.0));
    }

    public ClassificationResult classifyFromAveragePower(
            String athleteId, Minutes total, Watts averagePower, SeilerThresholds thresholds) {
        Minutes z1 = Minutes.of(0);
        Minutes z2 = Minutes.of(0);
        Minutes z3 = Minutes.of(0);

        if (averagePower.value() < thresholds.lt1Watts().value()) {
            z1 = total;
        } else if (averagePower.value() < thresholds.lt2Watts().value()) {
            z2 = total;
        } else {
            z3 = total;
        }

        return new ClassificationResult(
                athleteId,
                new SeilerZoneDistribution(z1, z2, z3),
                "avg_power_proxy",
                thresholds.lt1Watts(),
                thresholds.lt2Watts(),
                Math.min(thresholds.confidence(), 0.25));
    }
}

