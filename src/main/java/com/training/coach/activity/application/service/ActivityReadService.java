package com.training.coach.activity.application.service;

import com.training.coach.activity.application.port.out.ActivityRepository;
import com.training.coach.activity.domain.model.ActivityLight;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class ActivityReadService {

    private static final int MAX_RANGE_DAYS = 365;

    private final ActivityRepository activityRepository;

    public ActivityReadService(ActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
    }

    public List<ActivityLight> getActivities(String athleteId, LocalDate startDate, LocalDate endDate) {
        validateRange(startDate, endDate);
        return activityRepository.findByAthleteIdAndDateRange(athleteId, startDate, endDate);
    }

    public Optional<ActivityLight> getActivityByDate(String athleteId, LocalDate date) {
        return activityRepository.findByAthleteIdAndDate(athleteId, date);
    }

    private void validateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start date and end date are required");
        }
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("End date must be on or after start date");
        }
        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        if (daysBetween > MAX_RANGE_DAYS) {
            throw new IllegalArgumentException("Date range cannot exceed " + MAX_RANGE_DAYS + " days");
        }
    }
}
