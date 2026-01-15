package com.training.coach.trainingplan.presentation;

import com.training.coach.athlete.domain.model.Athlete;
import com.training.coach.athlete.domain.model.TrainingPlan;
import com.training.coach.shared.domain.unit.Hours;
import com.training.coach.trainingplan.application.service.TrainingPlanService;
import java.time.LocalDate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/training-plans")
public class TrainingPlanController {

    private final TrainingPlanService trainingPlanService;

    public TrainingPlanController(TrainingPlanService trainingPlanService) {
        this.trainingPlanService = trainingPlanService;
    }

    @PostMapping("/generate")
    public ResponseEntity<TrainingPlan> generatePlan(@RequestBody GeneratePlanRequest request) {
        // For MVP, assume athlete is provided or fetched
        // Here, create a dummy athlete or fetch by ID
        // For simplicity, use the athlete from request
        TrainingPlan plan = trainingPlanService.generatePlan(
                request.athlete(), request.phase(), request.startDate(), request.targetWeeklyHours());
        return ResponseEntity.ok(plan);
    }

    public record GeneratePlanRequest(Athlete athlete, String phase, LocalDate startDate, Hours targetWeeklyHours) {}
}
