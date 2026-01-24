package com.training.coach.analysis.domain.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AdjustmentProposalTest {

    @Test
    void createProposal_shouldCreateValidProposal() {
        // When
        AdjustmentProposal proposal = AdjustmentProposal.create(
                "plan-123",
                "athlete-123",
                AdjustmentProposal.AdjustmentType.REDUCE_INTENSITY,
                "Reduce intensity by 1 level",
                Map.of("intensityReduction", 1, "durationDays", 7),
                "coach"
        );

        // Then
        assertThat(proposal.planId()).isEqualTo("plan-123");
        assertThat(proposal.athleteId()).isEqualTo("athlete-123");
        assertThat(proposal.type()).isEqualTo(AdjustmentProposal.AdjustmentType.REDUCE_INTENSITY);
        assertThat(proposal.status()).isEqualTo(AdjustmentProposal.ProposalStatus.PENDING);
        assertThat(proposal.isPending()).isTrue();
        assertThat(proposal.isApproved()).isFalse();
        assertThat(proposal.isRejected()).isFalse();
    }

    @Test
    void approveProposal_shouldChangeStatusToApproved() {
        // Given
        AdjustmentProposal proposal = AdjustmentProposal.create(
                "plan-123",
                "athlete-123",
                AdjustmentProposal.AdjustmentType.REDUCE_VOLUME,
                "Reduce volume",
                Map.of(),
                "coach"
        );

        // When
        AdjustmentProposal approved = proposal.approve("head-coach");

        // Then
        assertThat(approved.status()).isEqualTo(AdjustmentProposal.ProposalStatus.APPROVED);
        assertThat(approved.isApproved()).isTrue();
        assertThat(approved.isPending()).isFalse();
        assertThat(approved.reviewedBy()).isEqualTo("head-coach");
        assertThat(approved.reviewedAt()).isNotNull();
    }

    @Test
    void rejectProposal_shouldChangeStatusToRejected() {
        // Given
        AdjustmentProposal proposal = AdjustmentProposal.create(
                "plan-123",
                "athlete-123",
                AdjustmentProposal.AdjustmentType.INCREASE_VOLUME,
                "Increase volume",
                Map.of(),
                "coach"
        );

        // When
        AdjustmentProposal rejected = proposal.reject("head-coach", "Exceeds safe load ramp");

        // Then
        assertThat(rejected.status()).isEqualTo(AdjustmentProposal.ProposalStatus.REJECTED);
        assertThat(rejected.isRejected()).isTrue();
        assertThat(rejected.isPending()).isFalse();
        assertThat(rejected.reviewedBy()).isEqualTo("head-coach");
        assertThat(rejected.rejectionReason()).isEqualTo("Exceeds safe load ramp");
    }

    @Test
    void markAsApplied_shouldChangeStatusToApplied() {
        // Given
        AdjustmentProposal proposal = AdjustmentProposal.create(
                "plan-123",
                "athlete-123",
                AdjustmentProposal.AdjustmentType.ADD_REST_DAY,
                "Add rest day",
                Map.of(),
                "coach"
        ).approve("head-coach");

        // When
        AdjustmentProposal applied = proposal.markAsApplied();

        // Then
        assertThat(applied.status()).isEqualTo(AdjustmentProposal.ProposalStatus.APPLIED);
    }

    @Test
    void guardrailCheckResult_passed_shouldCreatePassedResult() {
        // When
        AdjustmentProposal.GuardrailCheckResult result = AdjustmentProposal.GuardrailCheckResult.passed();

        // Then
        assertThat(result.passed()).isTrue();
        assertThat(result.blocked()).isFalse();
        assertThat(result.blockingRule()).isNull();
        assertThat(result.warningMessage()).isNull();
    }

    @Test
    void guardrailCheckResult_blocked_shouldCreateBlockedResult() {
        // When
        AdjustmentProposal.GuardrailCheckResult result =
                AdjustmentProposal.GuardrailCheckResult.blocked("LOAD_RAMP_EXCEEDED", "Weekly load increase exceeds safe limit");

        // Then
        assertThat(result.passed()).isFalse();
        assertThat(result.blocked()).isTrue();
        assertThat(result.blockingRule()).isEqualTo("LOAD_RAMP_EXCEEDED");
        assertThat(result.warningMessage()).isEqualTo("Weekly load increase exceeds safe limit");
    }

    @Test
    void createProposal_withInvalidPlanId_shouldThrowException() {
        // When/Then
        assertThatThrownBy(() ->
            new AdjustmentProposal(
                "   ",
                "athlete-123",
                AdjustmentProposal.AdjustmentType.REDUCE_INTENSITY,
                "desc",
                Map.of(),
                AdjustmentProposal.ProposalStatus.PENDING,
                Instant.now(),
                "coach",
                null,
                null,
                null,
                null
            )
        ).isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Plan ID cannot be null or blank");
    }

    @Test
    void createProposal_withNullType_shouldThrowException() {
        // When/Then
        assertThatThrownBy(() ->
            new AdjustmentProposal(
                "plan-123",
                "athlete-123",
                null,
                "desc",
                Map.of(),
                AdjustmentProposal.ProposalStatus.PENDING,
                Instant.now(),
                "coach",
                null,
                null,
                null,
                null
            )
        ).isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Adjustment type cannot be null");
    }

    @Test
    void guardrailCheckResult_warning_shouldCreateWarningResult() {
        // When
        AdjustmentProposal.GuardrailCheckResult result =
                AdjustmentProposal.GuardrailCheckResult.warning("Consider monitoring athlete closely");

        // Then
        assertThat(result.passed()).isTrue();
        assertThat(result.blocked()).isFalse();
        assertThat(result.warningMessage()).isEqualTo("Consider monitoring athlete closely");
    }
}