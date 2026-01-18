package com.training.coach.feedback.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Note Service Tests")
class NoteServiceTest {

    @Test
    @DisplayName("Should add and retrieve note")
    void shouldAddAndRetrieveNote() {
        // Given
        NoteService service = new NoteService();
        String athleteId = "test-athlete";
        String note = "Great session today!";

        // When
        service.addNote(athleteId, note);
        var notes = service.getNotes(athleteId);

        // Then
        assertThat(notes).contains(note);
    }
}
