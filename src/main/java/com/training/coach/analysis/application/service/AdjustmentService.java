package com.training.coach.analysis.application.service;

import org.springframework.stereotype.Service;

/**
 * Application service for suggesting plan adjustments based on readiness and compliance.
 */
@Service
public class AdjustmentService {

    public String suggestAdjustment(double readinessScore, double compliance) {
        if (readinessScore < 5.0) {
            return "Reduce volume by 20-30% and focus on recovery. Consider swapping high-intensity sessions.";
        } else if (compliance < 80.0) {
            return "Increase motivation cues or adjust schedule. Review workout accessibility.";
        } else {
            return "Maintain current plan. Athlete is performing well.";
        }
    }
}
