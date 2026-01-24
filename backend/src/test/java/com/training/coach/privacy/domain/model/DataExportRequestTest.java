package com.training.coach.privacy.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DataExportRequestTest {

    @Test
    void shouldCreateDataExportRequest() {
        DataExportRequest request = DataExportRequest.create("athlete-123");

        assertThat(request.id()).isNotEmpty();
        assertThat(request.athleteId()).isEqualTo("athlete-123");
        assertThat(request.status()).isEqualTo(DataExportRequest.RequestStatus.PENDING);
        assertThat(request.requestedAt()).isNotNull();
        assertThat(request.isPending()).isTrue();
    }

    @Test
    void shouldApprovePendingRequest() {
        DataExportRequest request = DataExportRequest.create("athlete-123");
        DataExportRequest approved = request.approve("admin-user");

        assertThat(approved.status()).isEqualTo(DataExportRequest.RequestStatus.APPROVED);
        assertThat(approved.approvedAt()).isNotNull();
        assertThat(approved.approvedBy()).isEqualTo("admin-user");
        assertThat(approved.isApproved()).isTrue();
    }

    @Test
    void shouldRejectPendingRequest() {
        DataExportRequest request = DataExportRequest.create("athlete-123");
        DataExportRequest rejected = request.reject("Request cannot be processed");

        assertThat(rejected.status()).isEqualTo(DataExportRequest.RequestStatus.REJECTED);
        assertThat(rejected.rejectionReason()).isEqualTo("Request cannot be processed");
    }

    @Test
    void shouldCompleteApprovedRequest() {
        DataExportRequest request = DataExportRequest.create("athlete-123");
        DataExportRequest approved = request.approve("admin-user");
        DataExportRequest completed = approved.complete("/exports/athlete_123.zip");

        assertThat(completed.status()).isEqualTo(DataExportRequest.RequestStatus.COMPLETED);
        assertThat(completed.exportFilePath()).isEqualTo("/exports/athlete_123.zip");
        assertThat(completed.completedAt()).isNotNull();
        assertThat(completed.isCompleted()).isTrue();
    }

    @Test
    void shouldThrowExceptionWhenApprovingNonPendingRequest() {
        DataExportRequest request = DataExportRequest.create("athlete-123");
        DataExportRequest approved = request.approve("admin-user");

        assertThatThrownBy(() -> approved.approve("another-admin"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Can only approve pending requests");
    }

    @Test
    void shouldThrowExceptionWhenRejectingNonPendingRequest() {
        DataExportRequest request = DataExportRequest.create("athlete-123");
        DataExportRequest approved = request.approve("admin-user");

        assertThatThrownBy(() -> approved.reject("Reason"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Can only reject pending requests");
    }

    @Test
    void shouldThrowExceptionWhenCompletingNonApprovedRequest() {
        DataExportRequest request = DataExportRequest.create("athlete-123");

        assertThatThrownBy(() -> request.complete("/exports/file.zip"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Can only complete approved requests");
    }
}
