package com.training.coach.analysis.presentation;

import com.training.coach.analysis.application.service.SeilerIntensityClassificationService;
import com.training.coach.analysis.application.service.SeilerThresholdService;
import com.training.coach.analysis.application.service.SeilerThresholds;
import com.training.coach.analysis.application.service.SeilerWorkoutPrescriptionService;
import com.training.coach.analysis.application.service.ThresholdMethod;
import com.training.coach.analysis.application.service.WorkoutIntensityPrescription;
import com.training.coach.analysis.application.service.WorkoutIntensityPurpose;
import com.training.coach.shared.domain.unit.Minutes;
import com.training.coach.shared.domain.unit.Watts;
import java.time.LocalDate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/intensity")
public class IntensityController {

    private final SeilerThresholdService thresholdService;
    private final SeilerIntensityClassificationService classificationService;
    private final SeilerWorkoutPrescriptionService prescriptionService;

    public IntensityController(
            SeilerThresholdService thresholdService,
            SeilerIntensityClassificationService classificationService,
            SeilerWorkoutPrescriptionService prescriptionService) {
        this.thresholdService = thresholdService;
        this.classificationService = classificationService;
        this.prescriptionService = prescriptionService;
    }

    @PutMapping("/athletes/{athleteId}/thresholds")
    public ResponseEntity<SeilerThresholds> upsertThresholds(
            @PathVariable String athleteId, @RequestBody UpsertThresholdsRequest request) {
        SeilerThresholds thresholds = new SeilerThresholds(
                athleteId,
                request.lt1Watts(),
                request.lt2Watts(),
                request.lt1Bpm(),
                request.lt2Bpm(),
                request.effectiveDate(),
                request.method(),
                request.confidence());
        return ResponseEntity.ok(thresholdService.upsert(thresholds));
    }

    @GetMapping("/athletes/{athleteId}/thresholds")
    public ResponseEntity<SeilerThresholds> getThresholds(@PathVariable String athleteId) {
        return thresholdService
                .getForAthlete(athleteId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/athletes/{athleteId}/classify")
    public ResponseEntity<SeilerIntensityClassificationService.ClassificationResult> classify(
            @PathVariable String athleteId, @RequestBody ClassifyRequest request) {
        var thresholdsOpt = thresholdService.getForAthlete(athleteId);

        if (request.z1Minutes() != null && request.z2Minutes() != null && request.z3Minutes() != null) {
            return ResponseEntity.ok(classificationService.classifyFromExplicitTimeInZones(
                    athleteId, request.z1Minutes(), request.z2Minutes(), request.z3Minutes(), thresholdsOpt));
        }

        if (request.totalMinutes() != null && request.averagePower() != null && thresholdsOpt.isPresent()) {
            return ResponseEntity.ok(classificationService.classifyFromAveragePower(
                    athleteId, request.totalMinutes(), request.averagePower(), thresholdsOpt.get()));
        }

        return ResponseEntity.badRequest().build();
    }

    @PostMapping("/prescribe")
    public ResponseEntity<WorkoutIntensityPrescription> prescribe(@RequestBody PrescribeRequest request) {
        return ResponseEntity.ok(prescriptionService.prescribeByPurpose(request.purpose()));
    }

    @PostMapping("/classify-purpose")
    public ResponseEntity<ClassifyPurposeResponse> classifyPurpose(@RequestBody ClassifyPurposeRequest request) {
        var result = prescriptionService.classifyPurposeWithConfidence(request.percentFtp());
        return ResponseEntity.ok(new ClassifyPurposeResponse(result.purpose(), result.method(), result.confidence()));
    }

    public record UpsertThresholdsRequest(
            com.training.coach.shared.domain.unit.Watts lt1Watts,
            com.training.coach.shared.domain.unit.Watts lt2Watts,
            com.training.coach.shared.domain.unit.BeatsPerMinute lt1Bpm,
            com.training.coach.shared.domain.unit.BeatsPerMinute lt2Bpm,
            LocalDate effectiveDate,
            ThresholdMethod method,
            double confidence) {}

    public record ClassifyRequest(
            Minutes totalMinutes, Watts averagePower, Minutes z1Minutes, Minutes z2Minutes, Minutes z3Minutes) {}

    public record PrescribeRequest(WorkoutIntensityPurpose purpose) {}

    public record ClassifyPurposeRequest(double percentFtp) {}

    public record ClassifyPurposeResponse(
            WorkoutIntensityPurpose purpose, com.training.coach.analysis.application.service.PrescriptionMethod method, double confidence) {}
}
