package com.training.coach.analysis.application.service;

import org.springframework.stereotype.Service;

/**
 * Application service for tracking compliance and progress.
 */
@Service
public class ComplianceService {

    public double calculateCompliance(int plannedWorkouts, int completedWorkouts) {
        if (plannedWorkouts == 0) return 100.0;
        return (double) completedWorkouts / plannedWorkouts * 100.0;
    }
}
