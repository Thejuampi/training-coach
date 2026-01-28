package com.training.coach.workout.presentation;

import com.training.coach.shared.domain.model.AthleteId;
import com.training.coach.workout.application.service.WorkoutExecutionService;
import com.training.coach.workout.domain.ExecutionStatus;
import com.training.coach.workout.domain.SkipReason;
import com.training.coach.workout.domain.WorkoutExecution;
import com.training.coach.workout.domain.WorkoutFeedback;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST controller for workout execution operations.
 */
@RestController
@RequestMapping("/api/workouts")
public class WorkoutController {

    private final WorkoutExecutionService workoutExecutionService;

    public WorkoutController(WorkoutExecutionService workoutExecutionService) {
        this.workoutExecutionService = workoutExecutionService;
    }

    /**
     * Start a workout execution.
     */
    @PostMapping("/{athleteId}/workouts/{plannedWorkoutId}/start")
    public ResponseEntity<WorkoutExecution> startWorkout(
            @PathVariable String athleteId,
            @PathVariable String plannedWorkoutId,
            @RequestParam(required = false) LocalDateTime startTime) {

        var time = startTime != null ? startTime : LocalDateTime.now();
        var execution = workoutExecutionService.startWorkout(athleteId, plannedWorkoutId, time);
        return ResponseEntity.ok(execution);
    }

    /**
     * Complete a workout with duration and intensity data.
     */
    @PostMapping("/{athleteId}/workouts/{executionId}/complete")
    public ResponseEntity<WorkoutExecution> completeWorkout(
            @PathVariable String athleteId,
            @PathVariable String executionId,
            @RequestParam int durationMinutes,
            @RequestParam(required = false) Integer intensityMinutes,
            @RequestParam double rpe,
            @RequestParam(required = false) String feedback) {

        var completed = workoutExecutionService.completeWorkout(executionId, durationMinutes,
                intensityMinutes != null ? intensityMinutes : durationMinutes, rpe, feedback);
        return ResponseEntity.ok(completed);
    }

    /**
     * Skip a workout with a reason.
     */
    @PostMapping("/{athleteId}/workouts/{executionId}/skip")
    public ResponseEntity<WorkoutExecution> skipWorkout(
            @PathVariable String athleteId,
            @PathVariable String executionId,
            @RequestParam SkipReason skipReason,
            @RequestParam(required = false) String notes) {

        var skipped = workoutExecutionService.skipWorkout(executionId, skipReason, notes);
        return ResponseEntity.ok(skipped);
    }

    /**
     * Match a completed activity to a planned workout.
     */
    @PostMapping("/{athleteId}/workouts/match-activity")
    public ResponseEntity<WorkoutExecution> matchActivityToWorkout(
            @PathVariable String athleteId,
            @RequestParam String activityId) {

        var matched = workoutExecutionService.matchActivityToWorkout(athleteId, activityId);
        return ResponseEntity.ok(matched);
    }

    /**
     * Get workout executions for an athlete by date range.
     */
    @GetMapping("/{athleteId}/executions")
    public ResponseEntity<List<WorkoutExecution>> getWorkoutExecutions(
            @PathVariable String athleteId,
            @RequestParam(required = false) LocalDateTime start,
            @RequestParam(required = false) LocalDateTime end) {

        var startDate = start != null ? start : LocalDateTime.now().minusDays(7);
        var endDate = end != null ? end : LocalDateTime.now();

        var executions = workoutExecutionService.getWorkoutExecutionsByDateRange(athleteId, startDate, endDate);
        return ResponseEntity.ok(executions);
    }

    /**
     * Get pending workouts for an athlete.
     */
    @GetMapping("/{athleteId}/pending")
    public ResponseEntity<List<WorkoutExecution>> getPendingWorkouts(@PathVariable String athleteId) {
        var pending = workoutExecutionService.getPendingWorkouts(athleteId);
        return ResponseEntity.ok(pending);
    }

    /**
     * Save workout feedback.
     */
    @PostMapping("/{athleteId}/workouts/{executionId}/feedback")
    public ResponseEntity<WorkoutFeedback> saveFeedback(
            @PathVariable String athleteId,
            @PathVariable String executionId,
            @RequestParam double rpe,
            @RequestParam(required = false) Integer perceivedExertion,
            @RequestParam(required = false) String notes) {

        var feedback = workoutExecutionService.saveWorkoutFeedback(executionId, rpe, perceivedExertion, notes);
        return ResponseEntity.ok(feedback);
    }

    /**
     * Get workout feedback for a specific execution.
     */
    @GetMapping("/{athleteId}/workouts/{executionId}/feedback")
    public ResponseEntity<List<WorkoutFeedback>> getWorkoutFeedback(
            @PathVariable String athleteId,
            @PathVariable String executionId) {

        var feedback = workoutExecutionService.getWorkoutExecutionRepository().findFeedbackByWorkoutExecutionId(executionId);
        return ResponseEntity.ok(feedback);
    }

    /**
     * Get workout quality score for an execution.
     */
    @GetMapping("/{athleteId}/workouts/{executionId}/quality")
    public ResponseEntity<Double> getWorkoutQuality(
            @PathVariable String athleteId,
            @PathVariable String executionId) {

        var execution = workoutExecutionService.getWorkoutExecutionRepository().findById(executionId);
        if (execution.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        var quality = workoutExecutionService.calculateWorkoutQuality(execution.get());
        return ResponseEntity.ok(quality);
    }

    /**
     * Get all workout execution statuses.
     */
    @GetMapping("/statuses")
    public ResponseEntity<Map<String, String>> getExecutionStatuses() {
        var statuses = Map.of(
            "PLANNED", "Workout is planned but not yet executed",
            "IN_PROGRESS", "Workout is in progress",
            "COMPLETED", "Workout was completed successfully",
            "SKIPPED", "Workout was skipped",
            "PARTIAL", "Workout was partially completed",
            "FAILED", "Workout attempt failed"
        );
        return ResponseEntity.ok(statuses);
    }

    /**
     * Get all skip reasons with descriptions.
     */
    @GetMapping("/skip-reasons")
    public ResponseEntity<Map<String, String>> getSkipReasons() {
        Map<String, String> reasons = new java.util.HashMap<>();
        reasons.put("ILLNESS", "Medical illness or injury");
        reasons.put("FATIGUE", "Excessive fatigue or overtraining");
        reasons.put("PAIN", "Muscle or joint pain");
        reasons.put("WEATHER", "Inclement weather conditions");
        reasons.put("TRAVEL", "Travel or logistical issues");
        reasons.put("EQUIPMENT", "Equipment failure or unavailability");
        reasons.put("EXHAUSTED", "Completely exhausted from previous days");
        reasons.put("OVERREACHED", "Overreached from training load");
        reasons.put("PERSONAL", "Personal emergency or family matters");
        reasons.put("STRESSMENTAL", "Mental health or stress management");
        reasons.put("SUBSTITUTION", "Alternative activity planned by coach");
        reasons.put("TAPERING", "Following taper protocol");
        reasons.put("MISSED", "Missed without specific reason");
        return ResponseEntity.ok(reasons);
    }

}