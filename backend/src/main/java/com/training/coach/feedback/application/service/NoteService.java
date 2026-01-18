package com.training.coach.feedback.application.service;

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

    public void addNote(String athleteId, String note) {
        notes.computeIfAbsent(athleteId, k -> new ArrayList<>()).add(note);
    }

    public List<String> getNotes(String athleteId) {
        return notes.getOrDefault(athleteId, List.of());
    }
}
