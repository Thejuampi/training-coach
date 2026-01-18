package com.training.coach.wellness.application.service;

import com.training.coach.athlete.application.port.out.FitnessPlatformPort;
import com.training.coach.wellness.application.port.out.WellnessRepository;
import com.training.coach.wellness.domain.model.TrainingLoadSummary;
import com.training.coach.wellness.domain.model.WellnessSnapshot;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class TrainingLoadCalculator {

    private static final int CTL_DAYS = 42;
    private static final int ATL_DAYS = 7;
    private static final double INTENSITY_FACTOR_DEFAULT = 0.6;

    private final WellnessRepository wellnessRepository;
    private final FitnessPlatformPort fitnessPlatformPort;

    public TrainingLoadCalculator(WellnessRepository wellnessRepository, FitnessPlatformPort fitnessPlatformPort) {
        this.wellnessRepository = wellnessRepository;
        this.fitnessPlatformPort = fitnessPlatformPort;
    }

    public TrainingLoadSummary calculateTrainingLoadSummary(String athleteId, LocalDate date) {
        List<WellnessSnapshot> snapshots =
                wellnessRepository.findByAthleteIdAndDateRange(athleteId, date.minusDays(CTL_DAYS), date);

        double ctl = calculateCtl(snapshots, date);
        double atl = calculateAtl(snapshots, date);
        double tsb = ctl - atl;
        double tss = calculateTotalTss(snapshots, date);
        int trainingMinutes = calculateTotalTrainingMinutes(snapshots, date);

        return new TrainingLoadSummary(tss, ctl, atl, tsb, trainingMinutes);
    }

    public void calculateAndStoreTrainingLoads(String athleteId, LocalDate startDate, LocalDate endDate) {
        List<WellnessSnapshot> existingSnapshots =
                wellnessRepository.findByAthleteIdAndDateRange(athleteId, startDate, endDate);

        Map<LocalDate, WellnessSnapshot> snapshotMap = existingSnapshots.stream()
                .collect(Collectors.toMap(WellnessSnapshot::date, s -> s, (s1, s2) -> s1, ConcurrentHashMap::new));

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            TrainingLoadSummary loadSummary = calculateTrainingLoadSummary(athleteId, date);
            WellnessSnapshot snapshot = snapshotMap.get(date);

            if (snapshot != null) {
                WellnessSnapshot updatedSnapshot = new WellnessSnapshot(
                        snapshot.id(),
                        snapshot.athleteId(),
                        snapshot.date(),
                        snapshot.physiological(),
                        snapshot.subjective(),
                        loadSummary,
                        snapshot.readinessScore());

                wellnessRepository.save(updatedSnapshot);
            }
        }
    }

    private double calculateCtl(List<WellnessSnapshot> snapshots, LocalDate upToDate) {
        List<WellnessSnapshot> recentSnapshots = snapshots.stream()
                .filter(s -> !s.date().isAfter(upToDate))
                .filter(s -> s.loadSummary() != null)
                .collect(Collectors.toList());

        if (recentSnapshots.isEmpty()) {
            return 0.0;
        }

        double sum = 0.0;
        int count = 0;

        for (WellnessSnapshot snapshot : recentSnapshots) {
            LocalDate date = snapshot.date();
            long daysAgo = java.time.temporal.ChronoUnit.DAYS.between(date, upToDate);

            if (daysAgo < CTL_DAYS) {
                double tss =
                        snapshot.loadSummary() != null ? snapshot.loadSummary().tss() : 0.0;
                double weight = Math.exp(-daysAgo / (double) CTL_DAYS);
                sum += tss * weight;
                count++;
            }
        }

        return count > 0 ? sum / CTL_DAYS : 0.0;
    }

    private double calculateAtl(List<WellnessSnapshot> snapshots, LocalDate upToDate) {
        List<WellnessSnapshot> recentSnapshots = snapshots.stream()
                .filter(s -> !s.date().isAfter(upToDate))
                .filter(s -> s.loadSummary() != null)
                .collect(Collectors.toList());

        if (recentSnapshots.isEmpty()) {
            return 0.0;
        }

        double sum = 0.0;
        int count = 0;

        for (WellnessSnapshot snapshot : recentSnapshots) {
            LocalDate date = snapshot.date();
            long daysAgo = java.time.temporal.ChronoUnit.DAYS.between(date, upToDate);

            if (daysAgo < ATL_DAYS) {
                double tss =
                        snapshot.loadSummary() != null ? snapshot.loadSummary().tss() : 0.0;
                double weight = Math.exp(-daysAgo / (double) ATL_DAYS);
                sum += tss * weight;
                count++;
            }
        }

        return count > 0 ? sum / ATL_DAYS : 0.0;
    }

    private double calculateTotalTss(List<WellnessSnapshot> snapshots, LocalDate date) {
        return snapshots.stream()
                .filter(s -> s.date().equals(date))
                .filter(s -> s.loadSummary() != null)
                .mapToDouble(s -> s.loadSummary().tss())
                .findFirst()
                .orElse(0.0);
    }

    private int calculateTotalTrainingMinutes(List<WellnessSnapshot> snapshots, LocalDate date) {
        return snapshots.stream()
                .filter(s -> s.date().equals(date))
                .filter(s -> s.loadSummary() != null)
                .mapToInt(s -> s.loadSummary().trainingMinutes())
                .findFirst()
                .orElse(0);
    }
}
