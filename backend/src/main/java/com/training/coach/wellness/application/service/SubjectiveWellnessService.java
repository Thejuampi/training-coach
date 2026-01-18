package com.training.coach.wellness.application.service;

import com.training.coach.shared.domain.unit.BeatsPerMinute;
import com.training.coach.shared.domain.unit.HeartRateVariability;
import com.training.coach.shared.domain.unit.Hours;
import com.training.coach.shared.domain.unit.Kilograms;
import com.training.coach.wellness.application.port.out.WellnessRepository;
import com.training.coach.wellness.domain.model.PhysiologicalData;
import com.training.coach.wellness.domain.model.SleepMetrics;
import com.training.coach.wellness.domain.model.SubjectiveWellness;
import com.training.coach.wellness.domain.model.TrainingLoadSummary;
import com.training.coach.wellness.domain.model.WellnessSnapshot;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class SubjectiveWellnessService {

    private final WellnessRepository wellnessRepository;
    private final ReadinessCalculatorService readinessCalculator;

    public SubjectiveWellnessService(
            WellnessRepository wellnessRepository, ReadinessCalculatorService readinessCalculator) {
        this.wellnessRepository = wellnessRepository;
        this.readinessCalculator = readinessCalculator;
    }

    public WellnessSnapshot recordSubjectiveWellness(
            String athleteId,
            java.time.LocalDate date,
            SubjectiveWellness subjective,
            BeatsPerMinute restingHeartRate,
            HeartRateVariability hrv,
            Kilograms bodyWeightKg,
            Hours sleepHours,
            int sleepQuality,
            TrainingLoadSummary loadSummary) {

        SleepMetrics sleepMetrics = SleepMetrics.basic(sleepHours, sleepQuality);
        PhysiologicalData physiological = new PhysiologicalData(restingHeartRate, hrv, bodyWeightKg, sleepMetrics);

        double readinessScore = readinessCalculator.calculateReadiness(physiological, subjective, loadSummary);

        WellnessSnapshot snapshot =
                WellnessSnapshot.create(athleteId, date, physiological, subjective, loadSummary, readinessScore);

        return wellnessRepository.save(snapshot);
    }

    public Optional<WellnessSnapshot> getWellnessSnapshot(String athleteId, java.time.LocalDate date) {
        return wellnessRepository.findByAthleteIdAndDate(athleteId, date);
    }
}
