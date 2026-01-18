package com.training.coach.activity.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(
        name = "activity_light",
        indexes = {
            @Index(name = "idx_activity_athlete_date", columnList = "athlete_id, date"),
            @Index(name = "idx_activity_athlete_external", columnList = "athlete_id, external_activity_id")
        })
@org.hibernate.annotations.Cache(usage = org.hibernate.annotations.CacheConcurrencyStrategy.READ_WRITE)
public class ActivityLightEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private String id;

    @Column(name = "athlete_id", nullable = false, length = 255)
    private String athleteId;

    @Column(name = "external_activity_id", nullable = false, length = 255)
    private String externalActivityId;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "name", length = 255)
    private String name;

    @Column(name = "type", length = 100)
    private String type;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    @Column(name = "distance_km")
    private Double distanceKm;

    @Column(name = "average_power")
    private Double averagePower;

    @Column(name = "average_heart_rate")
    private Double averageHeartRate;

    @Column(name = "training_stress_score")
    private Double trainingStressScore;

    @Column(name = "intensity_factor")
    private Double intensityFactor;

    @Column(name = "normalized_power")
    private Double normalizedPower;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

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

    public String getExternalActivityId() {
        return externalActivityId;
    }

    public void setExternalActivityId(String externalActivityId) {
        this.externalActivityId = externalActivityId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(Integer durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public Double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(Double distanceKm) {
        this.distanceKm = distanceKm;
    }

    public Double getAveragePower() {
        return averagePower;
    }

    public void setAveragePower(Double averagePower) {
        this.averagePower = averagePower;
    }

    public Double getAverageHeartRate() {
        return averageHeartRate;
    }

    public void setAverageHeartRate(Double averageHeartRate) {
        this.averageHeartRate = averageHeartRate;
    }

    public Double getTrainingStressScore() {
        return trainingStressScore;
    }

    public void setTrainingStressScore(Double trainingStressScore) {
        this.trainingStressScore = trainingStressScore;
    }

    public Double getIntensityFactor() {
        return intensityFactor;
    }

    public void setIntensityFactor(Double intensityFactor) {
        this.intensityFactor = intensityFactor;
    }

    public Double getNormalizedPower() {
        return normalizedPower;
    }

    public void setNormalizedPower(Double normalizedPower) {
        this.normalizedPower = normalizedPower;
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
}
