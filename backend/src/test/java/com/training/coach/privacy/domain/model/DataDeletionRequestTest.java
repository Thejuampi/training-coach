package com.training.coach.privacy.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DataDeletionRequestTest {

    @Test
    void shouldCreateDataDeletionRequest() {
        DataDeletionRequest request = DataDeletionRequest.create("athlete-123");

        assertThat(request.id()).isNotEmpty();
        assertThat(request.athleteId()).isEqualTo("athlete-123");
        assertThat(request.status()).isEqualTo(DataDeletionRequest.RequestStatus.PENDING);
        assertThat(request.requestedAt()).isNotNull();
        assertThat(request.isPending()).isTrue();
        assertThat(request.recordsDeleted()).isEqualTo(0);
        assertThat(request.recordsAnonymized()).isEqualTo(0);
    }

    @Test
    void shouldApprovePendingRequest() {
        DataDeletionRequest request = DataDeletionRequest.create("athlete-123");
        DataDeletionRequest approved = request.approve("admin-user");

        assertThat(approved.status()).isEqualTo(DataDeletionRequest.RequestStatus.APPROVED);
        assertThat(approved.approvedAt()).isNotNull();
        assertThat(approved.approvedBy()).isEqualTo("admin-user");
        assertThat(approved.isApproved()).isTrue();
    }

    @Test
    void shouldRejectPendingRequest() {
        DataDeletionRequest request = DataDeletionRequest.create("athlete-123");
        DataDeletionRequest rejected = request.reject("Legal hold in place");

        assertThat(rejected.status()).isEqualTo(DataDeletionRequest.RequestStatus.REJECTED);
        assertThat(rejected.rejectionReason()).isEqualTo("Legal hold in place");
    }

    @Test
    void shouldCompleteApprovedRequest() {
        DataDeletionRequest request = DataDeletionRequest.create("athlete-123");
        DataDeletionRequest approved = request.approve("admin-user");
        DataDeletionRequest completed = approved.complete(100, 50);

        assertThat(completed.status()).isEqualTo(DataDeletionRequest.RequestStatus.COMPLETED);
        assertThat(completed.recordsDeleted()).isEqualTo(100);
        assertThat(completed.recordsAnonymized()).isEqualTo(50);
        assertThat(completed.totalRecordsProcessed()).isEqualTo(150);
        assertThat(completed.completedAt()).isNotNull();
        assertThat(completed.isCompleted()).isTrue();
    }

    @Test
    void shouldCalculateTotalRecordsProcessed() {
        DataDeletionRequest request = DataDeletionRequest.create("athlete-123");
        DataDeletionRequest approved = request.approve("admin-user");
        DataDeletionRequest completed = approved.complete(25, 75);

        assertThat(completed.totalRecordsProcessed()).isEqualTo(100);
    }

    @Test
    void shouldThrowExceptionWhenApprovingNonPendingRequest() {
        DataDeletionRequest request = DataDeletionRequest.create("athlete-123");
        DataDeletionRequest approved = request.approve("admin-user");

        assertThatThrownBy(() -> approved.approve("another-admin"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Can only approve pending requests");
    }

    @Test
    void shouldThrowExceptionWhenRejectingNonPendingRequest() {
        DataDeletionRequest request = DataDeletionRequest.create("athlete-123");
        DataDeletionRequest approved = request.approve("admin-user");

        assertThatThrownBy(() -> approved.reject("Reason"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Can only reject pending requests");
    }

    @Test
    void shouldThrowExceptionWhenCompletingNonApprovedRequest() {
        DataDeletionRequest request = DataDeletionRequest.create("athlete-123");

        assertThatThrownBy(() -> request.complete(100, 50))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Can only complete approved requests");
    }

    @Test
    void shouldThrowExceptionWhenCreatingWithNegativeRecordsDeleted() {
        assertThatThrownBy(() -> new DataDeletionRequest(
                "id",
                "athlete-123",
                DataDeletionRequest.RequestStatus.PENDING,
                java.time.Instant.now(),
                null,
                null,
                null,
                -1,
                0,
                null,
                DataDeletionRequest.RequestMetadata.now()
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Records deleted cannot be negative");
    }

    @Test
    void shouldThrowExceptionWhenCreatingWithNegativeRecordsAnonymized() {
        assertThatThrownBy(() -> new DataDeletionRequest(
                "id",
                "athlete-123",
                DataDeletionRequest.RequestStatus.PENDING,
                java.time.Instant.now(),
                null,
                null,
                null,
                0,
                -1,
                null,
                DataDeletionRequest.RequestMetadata.now()
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Records anonymized cannot be negative");
    }
}
