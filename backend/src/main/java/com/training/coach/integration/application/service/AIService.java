package com.training.coach.integration.application.service;

import org.springframework.stereotype.Service;

/**
 * Application service for AI-powered suggestions.
 */
@Service
public class AIService {

    private final ClaudeApiClient claudeApiClient;

    public AIService(ClaudeApiClient claudeApiClient) {
        this.claudeApiClient = claudeApiClient;
    }

    public String getSuggestion(String prompt) {
        return claudeApiClient.call(prompt);
    }
}
