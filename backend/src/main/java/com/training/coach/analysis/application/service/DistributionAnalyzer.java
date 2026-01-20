package com.training.coach.analysis.application.service;

import com.training.coach.analysis.domain.model.PolarizedDistribution;
import com.training.coach.analysis.domain.model.Zone;
import com.training.coach.shared.domain.unit.Minutes;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Service for analyzing training distribution and detecting patterns.
 */
@Service
public class DistributionAnalyzer {

    private static final double Z2_CREEP_THRESHOLD = 20.0;
    private static final double POLARIZED_Z1_MIN = 75.0;
    private static final double POLARIZED_Z2_MAX = 10.0;
    private static final double POLARIZED_Z3_MIN = 15.0;

    /**
     * Analyzes weekly zone distribution and checks for Z2 creep.
     *
     * @param weeklyZoneMinutes map of zone to minutes
     * @return analysis result including creep detection
     */
    public AnalysisResult analyzeWeeklyDistribution(Map<Zone, Minutes> weeklyZoneMinutes) {
        Minutes z1 = weeklyZoneMinutes.getOrDefault(Zone.Z1, Minutes.of(0));
        Minutes z2 = weeklyZoneMinutes.getOrDefault(Zone.Z2, Minutes.of(0));
        Minutes z3 = weeklyZoneMinutes.getOrDefault(Zone.Z3, Minutes.of(0));

        PolarizedDistribution distribution = PolarizedDistribution.fromMinutes(
                z1.value(), z2.value(), z3.value()
        );

        boolean hasZ2Creep = distribution.hasZ2Creep();
        boolean isPolarized = distribution.isPolarized();
        boolean isTempoHeavy = distribution.isTempoHeavy();
        boolean isThresholdFocus = distribution.isThresholdFocus();

        String recommendation = buildRecommendation(distribution, hasZ2Creep);

        return new AnalysisResult(distribution, hasZ2Creep, isPolarized, recommendation);
    }

    /**
     * Detects Z2 creep from a list of weekly distributions.
     *
     * @param weeklyDistributions list of weekly distributions
     * @return true if any week shows Z2 creep
     */
    public boolean detectZ2CreepOverWeek(List<PolarizedDistribution> weeklyDistributions) {
        return weeklyDistributions.stream()
                .anyMatch(PolarizedDistribution::hasZ2Creep);
    }

    /**
     * Checks if a plan follows polarized distribution targets.
     *
     * @param distribution the planned distribution
     * @return true if it meets polarized targets
     */
    public boolean meetsPolarizedTargets(PolarizedDistribution distribution) {
        return distribution.z1Percent() >= POLARIZED_Z1_MIN
                && distribution.z2Percent() <= POLARIZED_Z2_MAX
                && distribution.z3Percent() >= POLARIZED_Z3_MIN;
    }

    /**
     * Result of distribution analysis.
     */
    public record AnalysisResult(
            PolarizedDistribution distribution,
            boolean hasZ2Creep,
            boolean isPolarized,
            String recommendation
    ) {}

    private String buildRecommendation(PolarizedDistribution distribution, boolean hasZ2Creep) {
        if (hasZ2Creep) {
            return "Z2_CREEP: Zone 2 training exceeds 20%. Consider adding more polarized intervals.";
        }
        if (distribution.isPolarized()) {
            return "Good polarized distribution maintained.";
        }
        if (distribution.isTempoHeavy()) {
            return "Tempo-heavy distribution detected. Consider adding more high-intensity intervals.";
        }
        if (distribution.isThresholdFocus()) {
            return "Threshold-focused training. Ensure adequate recovery between sessions.";
        }
        return "Distribution within acceptable ranges.";
        }
    }
