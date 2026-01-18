package com.training.coach.athlete.presentation;

import com.training.coach.athlete.application.service.AthleteService;
import com.training.coach.athlete.domain.model.Athlete;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/athletes")
public class AthleteController {

    private final AthleteService athleteService;

    public AthleteController(AthleteService athleteService) {
        this.athleteService = athleteService;
    }

    @PostMapping
    public ResponseEntity<Athlete> createAthlete(@RequestBody Athlete athlete) {
        var result = athleteService.createAthlete(athlete.name(), athlete.profile(), athlete.preferences());
        if (result.isSuccess()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(result.value().get());
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Athlete> getAthlete(@PathVariable String id) {
        var result = athleteService.getAthlete(id);
        if (result.isSuccess()) {
            return ResponseEntity.ok(result.value().get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping
    public ResponseEntity<List<Athlete>> getAllAthletes() {
        return ResponseEntity.ok(athleteService.getAllAthletes());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Athlete> updateAthlete(@PathVariable String id, @RequestBody Athlete athlete) {
        var result = athleteService.updateAthlete(id, athlete);
        if (result.isSuccess()) {
            return ResponseEntity.ok(result.value().get());
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAthlete(@PathVariable String id) {
        var result = athleteService.deleteAthlete(id);
        if (result.isSuccess()) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
