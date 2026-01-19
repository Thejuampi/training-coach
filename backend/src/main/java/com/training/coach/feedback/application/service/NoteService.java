package com.training.coach.feedback.application.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * Application service for coach notes and feedback.
 */
@Service
public class NoteService {

    private final Map<String, List<String>> notes = new HashMap<>();
    private final Map<String, Map<LocalDate, List<String>>> dateLinkedNotes = new HashMap<>();

    public void addNote(String athleteId, String note) {
        notes.computeIfAbsent(athleteId, k -> new ArrayList<>()).add(note);
    }

    public void addNoteForDate(String athleteId, LocalDate date, String note) {
        dateLinkedNotes.computeIfAbsent(athleteId, k -> new HashMap<>())
            .computeIfAbsent(date, k -> new ArrayList<>())
            .add(note);
    }

    public List<String> getNotes(String athleteId) {
        return notes.getOrDefault(athleteId, List.of());
    }

    public List<String> getNotesForDate(String athleteId, LocalDate date) {
        return dateLinkedNotes.getOrDefault(athleteId, Map.of())
            .getOrDefault(date, List.of());
    }

    public Map<LocalDate, List<String>> getAllNotesForAthlete(String athleteId) {
        return dateLinkedNotes.getOrDefault(athleteId, Map.of());
    }
}
