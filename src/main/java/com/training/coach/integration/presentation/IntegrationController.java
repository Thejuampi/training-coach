package com.training.coach.integration.presentation;

import com.training.coach.integration.application.service.AIService;
import com.training.coach.integration.application.service.IntegrationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/integrations")
public class IntegrationController {

    private final IntegrationService integrationService;
    private final AIService aiService;

    public IntegrationController(IntegrationService integrationService, AIService aiService) {
        this.integrationService = integrationService;
        this.aiService = aiService;
    }

    @PostMapping("/intervals-icu")
    public ResponseEntity<Void> configureIntervalsIcu(@RequestBody ConfigRequest request) {
        integrationService.configureIntervalsIcu(request.apiKey());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/intervals-icu")
    public ResponseEntity<String> getIntervalsIcuConfig() {
        return ResponseEntity.ok(integrationService.getIntervalsIcuApiKey());
    }

    @PostMapping("/ai/suggestions")
    public ResponseEntity<String> getAISuggestion(@RequestBody AIPrompt request) {
        String suggestion = aiService.getSuggestion(request.prompt());
        return ResponseEntity.ok(suggestion);
    }

    public record ConfigRequest(String apiKey) {}

    public record AIPrompt(String prompt) {}
}
