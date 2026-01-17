package com.training.coach.wellness.application.service;

import com.training.coach.wellness.application.port.out.TrainingLoadRepository;
import com.training.coach.wellness.application.port.out.WellnessRepository;
import com.training.coach.wellness.domain.model.PhysiologicalData;
import com.training.coach.wellness.domain.model.SubjectiveWellness;
import com.training.coach.wellness.domain.model.WellnessSnapshot;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class WellnessSubmissionService {

    private final WellnessRepository wellnessRepository;
    private final ReadinessCalculatorService readinessCalculator;
    private final TrainingLoadRepository trainingLoadRepository;

    public WellnessSubmissionService(
            WellnessRepository wellnessRepository,
            ReadinessCalculatorService readinessCalculator,
            TrainingLoadRepository trainingLoadRepository) {
        this.wellnessRepository = wellnessRepository;
        this.readinessCalculator = readinessCalculator;
        this.trainingLoadRepository = trainingLoadRepository;
    }

    public WellnessSnapshot submitWellness(
            String athleteId, LocalDate date, SubjectiveWellness subjective, PhysiologicalData physiological) {

        var loadSummary = trainingLoadRepository.findTrainingLoadByAthleteIdAndDate(athleteId, date);

        double readinessScore =
                readinessCalculator.calculateReadiness(physiological, subjective, loadSummary.orElse(null));

        var snapshot = WellnessSnapshot.create(
                athleteId, date, physiological, subjective, loadSummary.orElse(null), readinessScore);

        return wellnessRepository.save(snapshot);
    }

    public List<WellnessSnapshot> getWellnessHistory(String athleteId, LocalDate startDate, LocalDate endDate) {
        return wellnessRepository.findByAthleteIdAndDateRange(athleteId, startDate, endDate);
    }

    public Optional<WellnessSnapshot> getLatestWellness(String athleteId) {
        return wellnessRepository.findLatestByAthleteId(athleteId);
    }

    public Optional<WellnessSnapshot> getWellnessByDate(String athleteId, LocalDate date) {
        return wellnessRepository.findByAthleteIdAndDate(athleteId, date);
    }

    public void deleteWellnessEntry(String athleteId, LocalDate date) {
        wellnessRepository.deleteByAthleteIdAndDate(athleteId, date);
    }
}
