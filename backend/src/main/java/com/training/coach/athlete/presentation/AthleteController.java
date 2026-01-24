package com.training.coach.athlete.presentation;

import com.training.coach.athlete.application.service.AthleteService;
import com.training.coach.athlete.application.service.EventService;
import com.training.coach.athlete.domain.model.Athlete;
import com.training.coach.athlete.domain.model.Event;
import com.training.coach.athlete.domain.model.Workout;
import com.training.coach.athlete.domain.model.UserPreferences;
import com.training.coach.trainingplan.application.service.PlanService;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/athletes")
public class AthleteController {

    private final AthleteService athleteService;
    private final PlanService planService;
    private final EventService eventService;

    public AthleteController(AthleteService athleteService, PlanService planService, EventService eventService) {
        this.athleteService = athleteService;
        this.planService = planService;
        this.eventService = eventService;
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

    /**
     * View today's workout.
     */
    @GetMapping("/{id}/workout/today")
    public ResponseEntity<DailyWorkoutResponse> getTodayWorkout(@PathVariable String id) {
        LocalDate today = LocalDate.now();
        var workout = planService.getWorkoutForDate(id, today);

        if (workout == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        var response = new DailyWorkoutResponse(
                workout.type(),
                workout.durationMinutes(),
                workout.intensityProfile()
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Update measurement units and privacy settings.
     */
    @PutMapping("/{id}/preferences")
    public ResponseEntity<Athlete> updatePreferences(@PathVariable String id, @RequestBody UserPreferences preferences) {
        var result = athleteService.updateAthlete(id,
                new Athlete(id, null, null, null, preferences));

        if (result.isSuccess()) {
            return ResponseEntity.ok(result.value().get());
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Add a goal event.
     */
    @PostMapping("/{id}/events")
    public ResponseEntity<Event> addGoalEvent(@PathVariable String id, @RequestBody CreateEventRequest request) {
        var event = new Event(
                java.util.UUID.randomUUID().toString(),
                id,
                request.name(),
                LocalDate.parse(request.date()),
                request.priority()
        );

        // In a real implementation, this would save to a repository
        return ResponseEntity.status(HttpStatus.CREATED).body(event);
    }

    // Request/Response DTOs
    public record DailyWorkoutResponse(
            Workout.WorkoutType type,
            com.training.coach.shared.domain.unit.Minutes duration,
            Workout.IntensityProfile intensityProfile) {}

    public record CreateEventRequest(String name, String date, String priority) {}
}
