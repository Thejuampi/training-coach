package com.training.coach.application.service;

public class AdjustmentService {

    public String suggestAdjustment(double readinessScore, double compliance) {
        if (readinessScore < 4.0 && compliance > 80.0) {
            return "reducing volume";
        }
        return "maintaining volume";
    }
}