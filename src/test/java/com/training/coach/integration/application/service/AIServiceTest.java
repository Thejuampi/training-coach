package com.training.coach.integration.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("AI Service Tests")
class AIServiceTest {

    @Mock
    private ClaudeApiClient claudeApiClient;

    @InjectMocks
    private AIService aiService;

    @Test
    @DisplayName("Should get AI suggestion")
    void shouldGetAISuggestion() {
        // Given
        String prompt = "Suggest training adjustment";
        when(claudeApiClient.call(prompt)).thenReturn("Reduce volume by 10%");

        // When
        String suggestion = aiService.getSuggestion(prompt);

        // Then
        assertThat(suggestion).isEqualTo("Reduce volume by 10%");
    }
}
