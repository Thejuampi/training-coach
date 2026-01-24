package com.training.coach.athlete.application.service;

import com.training.coach.athlete.application.port.out.AthleteRepository;
import com.training.coach.athlete.domain.model.Athlete;
import com.training.coach.athlete.domain.model.AthleteProfile;
import com.training.coach.athlete.domain.model.TrainingMetrics;
import com.training.coach.athlete.domain.model.TrainingPreferences;
import com.training.coach.feedback.application.service.NoteService;
import com.training.coach.shared.domain.unit.BeatsPerMinute;
import com.training.coach.shared.domain.unit.Vo2Max;
import com.training.coach.shared.domain.unit.Watts;
import com.training.coach.shared.exception.AthleteNotFoundException;
import com.training.coach.shared.functional.Result;
import com.training.coach.wellness.application.port.out.WellnessRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * Application service for athlete operations.
 */
@Service
public class AthleteService {

    private final AthleteRepository athleteRepository;
    private final WellnessRepository wellnessRepository;
    private final NoteService noteService;
    private final com.training.coach.activity.application.port.out.ActivityRepository activityRepository;

    public AthleteService(AthleteRepository athleteRepository, WellnessRepository wellnessRepository, NoteService noteService,
                         com.training.coach.activity.application.port.out.ActivityRepository activityRepository) {
        this.athleteRepository = athleteRepository;
        this.wellnessRepository = wellnessRepository;
        this.noteService = noteService;
        this.activityRepository = activityRepository;
    }

    public Result<Athlete> createAthlete(String name, AthleteProfile profile, TrainingPreferences preferences) {
        String id = UUID.randomUUID().toString();
        // Initial metrics with reasonable defaults
        TrainingMetrics initialMetrics =
                new TrainingMetrics(Watts.of(200.0), BeatsPerMinute.of(160.0), Vo2Max.of(40.0), profile.weightKg());
        Athlete athlete = new Athlete(id, name, profile, initialMetrics, preferences);
        Athlete saved = athleteRepository.save(athlete);
        return Result.success(saved);
    }

    public Result<Athlete> getAthlete(String id) {
        return athleteRepository
                .findById(id)
                .map(Result::success)
                .orElse(Result.failure(new AthleteNotFoundException(id)));
    }

    public Result<Athlete> updateAthlete(String id, Athlete updatedAthlete) {
        return getAthlete(id).flatMap(existing -> {
            Athlete athleteToSave = new Athlete(
                    id,
                    updatedAthlete.name(),
                    updatedAthlete.profile(),
                    updatedAthlete.currentMetrics(),
                    updatedAthlete.preferences());
            Athlete saved = athleteRepository.save(athleteToSave);
            return Result.success(saved);
        });
    }

    public Result<Void> deleteAthlete(String id) {
        // Delete associated data first
        activityRepository.deleteByAthleteId(id);
        wellnessRepository.deleteByAthleteId(id);
        noteService.deleteNotesForAthlete(id);

        // Then delete the athlete
        athleteRepository.deleteById(id);
        return Result.success(null);
    }

    public java.util.List<Athlete> getAllAthletes() {
        return athleteRepository.findAll();
    }
}
