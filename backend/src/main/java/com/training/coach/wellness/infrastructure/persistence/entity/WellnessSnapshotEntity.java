package com.training.coach.wellness.infrastructure.persistence.entity;

import com.training.coach.shared.domain.unit.BeatsPerMinute;
import com.training.coach.shared.domain.unit.Grams;
import com.training.coach.shared.domain.unit.HeartRateVariability;
import com.training.coach.shared.domain.unit.Hours;
import com.training.coach.shared.persistence.converter.BeatsPerMinuteConverter;
import com.training.coach.shared.persistence.converter.GramsConverter;
import com.training.coach.shared.persistence.converter.HeartRateVariabilityConverter;
import com.training.coach.shared.persistence.converter.HoursConverter;
import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(
        name = "wellness_snapshots",
        indexes = {
            @Index(name = "idx_wellness_athlete_date", columnList = "athlete_id, date"),
            @Index(name = "idx_wellness_athlete_id", columnList = "athlete_id")
        })
@org.hibernate.annotations.Cache(usage = org.hibernate.annotations.CacheConcurrencyStrategy.READ_WRITE)
public class WellnessSnapshotEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private String id;

    @Column(name = "athlete_id", nullable = false, length = 255)
    private String athleteId;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "source", length = 50)
    private String source;

    @Convert(converter = BeatsPerMinuteConverter.class)
    @Column(name = "resting_heart_rate")
    private BeatsPerMinute restingHeartRate;

    @Convert(converter = HeartRateVariabilityConverter.class)
    @Column(name = "hrv")
    private HeartRateVariability hrv;

    @Convert(converter = GramsConverter.class)
    @Column(name = "body_weight_g")
    private Grams bodyWeightGrams;

    @Convert(converter = HoursConverter.class)
    @Column(name = "sleep_hours")
    private Hours sleepHours;

    @Column(name = "sleep_quality")
    private Integer sleepQuality;

    @Column(name = "fatigue")
    private Integer fatigue;

    @Column(name = "stress")
    private Integer stress;

    @Column(name = "motivation")
    private Integer motivation;

    @Column(name = "soreness")
    private Integer soreness;

    @Column(name = "readiness_score")
    private Double readinessScore;

    @Column(name = "tss")
    private Double tss;

    @Column(name = "ctl")
    private Double ctl;

    @Column(name = "atl")
    private Double atl;

    @Column(name = "tsb")
    private Double tsb;

    @Column(name = "training_minutes")
    private Integer trainingMinutes;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAthleteId() {
        return athleteId;
    }

    public void setAthleteId(String athleteId) {
        this.athleteId = athleteId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public BeatsPerMinute getRestingHeartRate() {
        return restingHeartRate;
    }

    public void setRestingHeartRate(BeatsPerMinute restingHeartRate) {
        this.restingHeartRate = restingHeartRate;
    }

    public HeartRateVariability getHrv() {
        return hrv;
    }

    public void setHrv(HeartRateVariability hrv) {
        this.hrv = hrv;
    }

    public Grams getBodyWeightGrams() {
        return bodyWeightGrams;
    }

    public void setBodyWeightGrams(Grams bodyWeightGrams) {
        this.bodyWeightGrams = bodyWeightGrams;
    }

    public Hours getSleepHours() {
        return sleepHours;
    }

    public void setSleepHours(Hours sleepHours) {
        this.sleepHours = sleepHours;
    }

    public Integer getSleepQuality() {
        return sleepQuality;
    }

    public void setSleepQuality(Integer sleepQuality) {
        this.sleepQuality = sleepQuality;
    }

    public Integer getFatigue() {
        return fatigue;
    }

    public void setFatigue(Integer fatigue) {
        this.fatigue = fatigue;
    }

    public Integer getStress() {
        return stress;
    }

    public void setStress(Integer stress) {
        this.stress = stress;
    }

    public Integer getMotivation() {
        return motivation;
    }

    public void setMotivation(Integer motivation) {
        this.motivation = motivation;
    }

    public Integer getSoreness() {
        return soreness;
    }

    public void setSoreness(Integer soreness) {
        this.soreness = soreness;
    }

    public Double getReadinessScore() {
        return readinessScore;
    }

    public void setReadinessScore(Double readinessScore) {
        this.readinessScore = readinessScore;
    }

    public Double getTss() {
        return tss;
    }

    public void setTss(Double tss) {
        this.tss = tss;
    }

    public Double getCtl() {
        return ctl;
    }

    public void setCtl(Double ctl) {
        this.ctl = ctl;
    }

    public Double getAtl() {
        return atl;
    }

    public void setAtl(Double atl) {
        this.atl = atl;
    }

    public Double getTsb() {
        return tsb;
    }

    public void setTsb(Double tsb) {
        this.tsb = tsb;
    }

    public Integer getTrainingMinutes() {
        return trainingMinutes;
    }

    public void setTrainingMinutes(Integer trainingMinutes) {
        this.trainingMinutes = trainingMinutes;
    }
}
