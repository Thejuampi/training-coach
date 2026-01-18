package com.training.coach.analysis.application.service;

import java.util.Objects;
import org.springframework.stereotype.Service;

@Service
public class SeilerWorkoutPrescriptionService {

    /**
     * Returns a pragmatic default %FTP range for the desired purpose.
     *
     * <p>These are guidance bands for workout prescription; distribution is still tracked using LT1/LT2 anchored
     * Seiler Z1/Z2/Z3.
     */
    public WorkoutIntensityPrescription prescribeByPurpose(WorkoutIntensityPurpose purpose) {
        Objects.requireNonNull(purpose, "purpose");

        return switch (purpose) {
            case Z1_RECOVERY -> new WorkoutIntensityPrescription(
                    purpose, 0.40, 0.55, PrescriptionMethod.PERCENT_FTP_PROXY, 0.3, "Very easy recovery below LT1");
            case Z1_ENDURANCE -> new WorkoutIntensityPrescription(
                    purpose,
                    0.56,
                    0.73,
                    PrescriptionMethod.PERCENT_FTP_PROXY,
                    0.3,
                    "Steady aerobic endurance below LT1");
            case Z1_FATMAX -> new WorkoutIntensityPrescription(
                    purpose,
                    0.74,
                    0.82,
                    PrescriptionMethod.PERCENT_FTP_PROXY,
                    0.2,
                    "Upper Z1 band below LT1, often near FATMAX for an athlete");
            case Z2_DISCOURAGED_TEMPO -> new WorkoutIntensityPrescription(
                    purpose,
                    0.83,
                    0.87,
                    PrescriptionMethod.PERCENT_FTP_PROXY,
                    0.2,
                    "Discouraged tempo band in polarized models; use sparingly");
            case Z2_THRESHOLD -> new WorkoutIntensityPrescription(
                    purpose,
                    0.88,
                    1.00,
                    PrescriptionMethod.PERCENT_FTP_PROXY,
                    0.3,
                    "Between LT1 and LT2; minimize in polarized models");
            case Z3_VO2_OPTIMAL -> new WorkoutIntensityPrescription(
                    purpose,
                    1.05,
                    1.15,
                    PrescriptionMethod.PERCENT_FTP_PROXY,
                    0.4,
                    "VO2-oriented intervals (cycling), distinct from sprint work");
            case Z3_SPRINT -> new WorkoutIntensityPrescription(
                    purpose,
                    1.16,
                    2.00,
                    PrescriptionMethod.PERCENT_FTP_PROXY,
                    0.4,
                    "Sprint/neuromuscular work above VO2-optimal band");
        };
    }

    /**
     * Classifies an interval target by %FTP into a purpose label.
     *
     * <p>Rule of thumb (cycling):
     *
     * <ul>
     *   <li>VO2-optimal: 105-115% FTP
     *   <li>Sprint: above 115% FTP
     * </ul>
     */
    public WorkoutIntensityPurpose classifyPurposeByPercentFtp(double percentFtp) {
        if (percentFtp < 0) {
            throw new IllegalArgumentException("percentFtp must be non-negative");
        }

        if (percentFtp > 1.15) {
            return WorkoutIntensityPurpose.Z3_SPRINT;
        }
        if (percentFtp >= 1.05) {
            return WorkoutIntensityPurpose.Z3_VO2_OPTIMAL;
        }
        if (percentFtp >= 0.88) {
            return WorkoutIntensityPurpose.Z2_THRESHOLD;
        }
        if (percentFtp >= 0.83) {
            return WorkoutIntensityPurpose.Z2_DISCOURAGED_TEMPO;
        }
        if (percentFtp >= 0.74) {
            return WorkoutIntensityPurpose.Z1_FATMAX;
        }
        if (percentFtp >= 0.56) {
            return WorkoutIntensityPurpose.Z1_ENDURANCE;
        }
        return WorkoutIntensityPurpose.Z1_RECOVERY;
    }

    public PurposeClassification classifyPurposeWithConfidence(double percentFtp) {
        return new PurposeClassification(
                classifyPurposeByPercentFtp(percentFtp), PrescriptionMethod.PERCENT_FTP_PROXY, 0.35);
    }

    public record PurposeClassification(
            WorkoutIntensityPurpose purpose, PrescriptionMethod method, double confidence) {}
}
