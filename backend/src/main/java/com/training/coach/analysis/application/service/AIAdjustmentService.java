package com.training.coach.analysis.application.service;

import com.training.coach.athlete.application.port.out.AthleteRepository;
import com.training.coach.athlete.application.port.out.WellnessRepository;
import com.training.coach.athlete.domain.model.Athlete;
import com.training.coach.analysis.domain.model.AdjustmentProposal;
import com.training.coach.analysis.domain.model.AdjustmentProposal.AdjustmentType;
import com.training.coach.integration.application.service.ClaudeAIClient;
import com.training.coach.wellness.domain.model.WellnessSnapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for generating AI-powered adjustment suggestions using Claude API.
 */
@Service
public class AIAdjustmentService {

    private static final Logger logger = LoggerFactory.getLogger(AIAdjustmentService.class);

    private final AthleteRepository athleteRepository;
    private final WellnessRepository wellnessRepository;
    private final SafetyGuardrailService safetyGuardrailService;
    private final ClaudeAIClient aiClient;

    public AIAdjustmentService(
            AthleteRepository athleteRepository,
            WellnessRepository wellnessRepository,
            SafetyGuardrailService safetyGuardrailService,
            ClaudeAIClient aiClient) {
        this.athleteRepository = athleteRepository;
        this.wellnessRepository = wellnessRepository;
        this.safetyGuardrailService = safetyGuardrailService;
        this.aiClient = aiClient;
    }

    /**
     * Generate adjustment suggestions based on athlete's recent wellness data.
     */
    public List<AdjustmentProposal> generateSuggestions(String athleteId, String planId) {
        Athlete athlete = athleteRepository.findById(athleteId)
                .orElseThrow(() -> new IllegalArgumentException("Athlete not found: " + athleteId));

        // Get recent wellness data
        List<WellnessSnapshot> recentWellness = wellnessRepository.findByAthleteId(athleteId).stream()
                .filter(w -> w.date().isAfter(LocalDate.now().minusDays(7)))
                .toList();

        // Calculate readiness trend
        double avgReadiness = recentWellness.stream()
                .mapToDouble(WellnessSnapshot::readinessScore)
                .average()
                .orElse(50.0);

        // Generate suggestions based on readiness
        if (avgReadiness < 40.0) {
            return generateLowReadinessSuggestions(athleteId, planId, avgReadiness);
        } else if (avgReadiness < 60.0) {
            return generateModerateReadinessSuggestions(athleteId, planId, avgReadiness);
        } else {
            return List.of(); // No adjustments needed
        }
    }

    /**
     * Generate suggestions for low readiness athletes.
     */
    private List<AdjustmentProposal> generateLowReadinessSuggestions(
            String athleteId, String planId, double avgReadiness) {

        Map<String, Object> params = new HashMap<>();
        params.put("intensityReduction", 1);
        params.put("durationDays", 7);
        params.put("reason", "Low readiness: " + String.format("%.1f", avgReadiness));

        AdjustmentProposal proposal = AdjustmentProposal.create(
                planId,
                athleteId,
                AdjustmentType.REDUCE_INTENSITY,
                "Reduce workout intensity by 1 level for 7 days due to low readiness",
                params,
                "system"
        );

        // Run through guardrails
        var guardrailResult = checkGuardrailsForProposal(proposal);
        proposal = new AdjustmentProposal(
                proposal.id(),
                proposal.planId(),
                proposal.athleteId(),
                proposal.type(),
                proposal.description(),
                proposal.parameters(),
                proposal.status(),
                proposal.proposedAt(),
                proposal.proposedBy(),
                proposal.reviewedAt(),
                proposal.reviewedBy(),
                proposal.rejectionReason(),
                guardrailResult
        );

        return List.of(proposal);
    }

    /**
     * Generate suggestions for moderate readiness athletes.
     */
    private List<AdjustmentProposal> generateModerateReadinessSuggestions(
            String athleteId, String planId, double avgReadiness) {

        Map<String, Object> params = new HashMap<>();
        params.put("volumeReductionPercent", 15);
        params.put("durationDays", 3);
        params.put("reason", "Moderate readiness: " + String.format("%.1f", avgReadiness));

        AdjustmentProposal proposal = AdjustmentProposal.create(
                planId,
                athleteId,
                AdjustmentType.REDUCE_VOLUME,
                "Reduce training volume by 15% for 3 days",
                params,
                "system"
        );

        // Run through guardrails
        var guardrailResult = checkGuardrailsForProposal(proposal);
        proposal = new AdjustmentProposal(
                proposal.id(),
                proposal.planId(),
                proposal.athleteId(),
                proposal.type(),
                proposal.description(),
                proposal.parameters(),
                proposal.status(),
                proposal.proposedAt(),
                proposal.proposedBy(),
                proposal.reviewedAt(),
                proposal.reviewedBy(),
                proposal.rejectionReason(),
                guardrailResult
        );

        return List.of(proposal);
    }

    /**
     * Use AI to generate a custom adjustment suggestion.
     */
    public AdjustmentProposal generateAISuggestion(
            String athleteId,
            String planId,
            String context,
            String proposedBy
    ) {
        Athlete athlete = athleteRepository.findById(athleteId)
                .orElseThrow(() -> new IllegalArgumentException("Athlete not found: " + athleteId));

        // Build prompt for AI
        String prompt = buildAdjustmentPrompt(athlete, context);

        // Call AI for suggestion
        String aiResponse = aiClient.generateText(prompt);

        // Parse AI response into proposal
        return parseAIResponseToProposal(athleteId, planId, aiResponse, proposedBy);
    }

    /**
     * Check proposal against safety guardrails.
     */
    private AdjustmentProposal.GuardrailCheckResult checkGuardrailsForProposal(AdjustmentProposal proposal) {
        // Get current wellness data
        List<WellnessSnapshot> wellness = wellnessRepository.findByAthleteId(proposal.athleteId());
        if (wellness.isEmpty()) {
            return AdjustmentProposal.GuardrailCheckResult.warning(
                    "No wellness data available for guardrail check"
            );
        }

        WellnessSnapshot latest = wellness.stream()
                .max((w1, w2) -> w1.date().compareTo(w2.date()))
                .orElseThrow();

        // Check against guardrails based on adjustment type
        return switch (proposal.type()) {
            case REDUCE_INTENSITY, REDUCE_VOLUME -> {
                // Reducing load is generally safe
                yield AdjustmentProposal.GuardrailCheckResult.passed();
            }
            case INCREASE_VOLUME -> {
                // Increasing load needs guardrail check
                double proposedIncrease = ((Number) proposal.parameters()
                        .getOrDefault("volumeIncreasePercent", 0)).doubleValue();
                double currentLoad = 100.0; // Placeholder
                double proposedLoad = currentLoad * (1 + proposedIncrease / 100);

                var result = safetyGuardrailService.checkLoadRamp(
                        proposal.athleteId(),
                        currentLoad,
                        proposedLoad,
                        LocalDate.now()
                );

                if (result.blocked()) {
                    yield AdjustmentProposal.GuardrailCheckResult.blocked(
                            result.blockingRule(),
                            result.blockingReason()
                    );
                } else {
                    yield AdjustmentProposal.GuardrailCheckResult.passed();
                }
            }
            default -> AdjustmentProposal.GuardrailCheckResult.passed();
        };
    }

    /**
     * Build prompt for AI adjustment suggestion.
     */
    private String buildAdjustmentPrompt(Athlete athlete, String context) {
        return String.format("""
                You are an expert training coach. Based on the following context, suggest a training plan adjustment.

                Athlete: %s
                Context: %s

                Provide a suggestion in the following format:
                TYPE: [ADJUSTMENT_TYPE]
                DESCRIPTION: [Brief description]
                PARAMS: [key=value pairs]

                Available adjustment types: REDUCE_INTENSITY, REDUCE_VOLUME, INCREASE_VOLUME, SWAP_WORKOUT, ADD_REST_DAY
                """,
                athlete.name(),
                context
        );
    }

    /**
     * Parse AI response into an AdjustmentProposal.
     */
    private AdjustmentProposal parseAIResponseToProposal(
            String athleteId, String planId, String aiResponse, String proposedBy) {

        // Simple parsing - in production this would be more robust
        AdjustmentType type = AdjustmentType.CUSTOM;
        String description = aiResponse;
        Map<String, Object> params = new HashMap<>();
        params.put("aiGenerated", true);
        params.put("aiResponse", aiResponse);

        return AdjustmentProposal.create(planId, athleteId, type, description, params, proposedBy);
    }
}