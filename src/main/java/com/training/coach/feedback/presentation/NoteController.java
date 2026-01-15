package com.training.coach.feedback.presentation;

import com.training.coach.feedback.application.service.NoteService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notes")
public class NoteController {

    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    @PostMapping("/athletes/{athleteId}")
    public ResponseEntity<Void> addNote(@PathVariable String athleteId, @RequestBody NoteRequest request) {
        noteService.addNote(athleteId, request.note());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/athletes/{athleteId}")
    public ResponseEntity<List<String>> getNotes(@PathVariable String athleteId) {
        return ResponseEntity.ok(noteService.getNotes(athleteId));
    }

    public record NoteRequest(String note) {}
}
