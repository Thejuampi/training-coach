package com.training.coach.user.infrastructure.persistence.entity;

import com.training.coach.user.domain.model.DistanceUnit;
import com.training.coach.user.domain.model.HeightUnit;
import com.training.coach.user.domain.model.MeasurementSystem;
import com.training.coach.user.domain.model.UserRole;
import com.training.coach.user.domain.model.WeightUnit;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "system_users")
public class SystemUserEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private String id;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(name = "measurement_system", nullable = false, length = 20)
    private MeasurementSystem measurementSystem;

    @Enumerated(EnumType.STRING)
    @Column(name = "weight_unit", nullable = false, length = 20)
    private WeightUnit weightUnit;

    @Enumerated(EnumType.STRING)
    @Column(name = "distance_unit", nullable = false, length = 20)
    private DistanceUnit distanceUnit;

    @Enumerated(EnumType.STRING)
    @Column(name = "height_unit", nullable = false, length = 20)
    private HeightUnit heightUnit;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public MeasurementSystem getMeasurementSystem() {
        return measurementSystem;
    }

    public void setMeasurementSystem(MeasurementSystem measurementSystem) {
        this.measurementSystem = measurementSystem;
    }

    public WeightUnit getWeightUnit() {
        return weightUnit;
    }

    public void setWeightUnit(WeightUnit weightUnit) {
        this.weightUnit = weightUnit;
    }

    public DistanceUnit getDistanceUnit() {
        return distanceUnit;
    }

    public void setDistanceUnit(DistanceUnit distanceUnit) {
        this.distanceUnit = distanceUnit;
    }

    public HeightUnit getHeightUnit() {
        return heightUnit;
    }

    public void setHeightUnit(HeightUnit heightUnit) {
        this.heightUnit = heightUnit;
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
