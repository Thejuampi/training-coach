package com.training.coach.wellness.infrastructure.adapter;

import com.training.coach.wellness.application.port.out.TrainingLoadRepository;
import com.training.coach.wellness.domain.model.TrainingLoadSummary;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class TrainingLoadRepositoryAdapter implements TrainingLoadRepository {

    @Override
    public Optional<TrainingLoadSummary> findTrainingLoadByAthleteIdAndDate(String athleteId, LocalDate date) {
        double ctl = findCtlByAthleteIdAndDate(athleteId, date);
        double atl = findAtlByAthleteIdAndDate(athleteId, date);
        double tsb = ctl - atl;
        return Optional.of(new TrainingLoadSummary(0.0, ctl, atl, tsb, 0));
    }

    @Override
    public double findCtlByAthleteIdAndDate(String athleteId, LocalDate date) {
        return 0.0;
    }

    @Override
    public double findAtlByAthleteIdAndDate(String athleteId, LocalDate date) {
        return 0.0;
    }

    @Override
    public void saveCtl(String athleteId, LocalDate date, double ctl) {}

    @Override
    public void saveAtl(String athleteId, LocalDate date, double atl) {}
}
