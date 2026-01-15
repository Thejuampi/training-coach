package com.training.coach.integration.application.service;

import org.springframework.stereotype.Service;

/**
 * Application service for managing integrations.
 */
@Service
public class IntegrationService {

    private String intervalsIcuApiKey;

    public void configureIntervalsIcu(String apiKey) {
        this.intervalsIcuApiKey = apiKey;
        // In real app, persist to DB or config
    }

    public String getIntervalsIcuApiKey() {
        return intervalsIcuApiKey;
    }
}
