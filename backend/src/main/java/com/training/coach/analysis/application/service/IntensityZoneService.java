package com.training.coach.analysis.application.service;

import com.training.coach.analysis.domain.model.IntensityZones;
import com.training.coach.analysis.domain.model.Zone;
import com.training.coach.shared.domain.unit.Watts;
import org.springframework.stereotype.Service;

/**
 * Service for calculating and managing intensity zones based on LT1/LT2 thresholds.
 */
@Service
public class IntensityZoneService {

    private static final double LT1_TO_FTP_RATIO = 0.75;
    private static final double LT2_TO_FTP_RATIO = 0.90;

    /**
     * Creates IntensityZones from LT1 and LT2 watt thresholds.
     * Zone boundaries are derived using Seiler's 3-zone model.
     *
     * @param lt1Watts the LT1 threshold in watts
     * @param lt2Watts the LT2 threshold in watts
     * @return the calculated intensity zones
     */
    public IntensityZones calculateZones(Watts lt1Watts, Watts lt2Watts) {
        // Z1 upper bound is LT1
        Watts z1UpperBound = lt1Watts;

        // Z2 upper bound is LT2
        Watts z2UpperBound = lt2Watts;

        // Z3 upper bound is set to a reasonable multiple above LT2 (e.g., 150% of FTP)
        double ftpEstimate = lt1Watts.value() / LT1_TO_FTP_RATIO;
        Watts z3UpperBound = Watts.of(ftpEstimate * 1.5);

        return new IntensityZones(lt1Watts, lt2Watts, z1UpperBound, z2UpperBound, z3UpperBound);
    }

    /**
     * Estimates LT1 and LT2 from FTP.
     * LT1 ~75% of FTP, LT2 ~90% of FTP
     *
     * @param ftp the functional threshold power
     * @return SeilerThresholds with estimated values
     */
    public SeilerThresholds estimateThresholdsFromFtp(String athleteId, Watts ftp, ThresholdMethod method) {
        Watts estimatedLt1 = Watts.of(ftp.value() * LT1_TO_FTP_RATIO);
        Watts estimatedLt2 = Watts.of(ftp.value() * LT2_TO_FTP_RATIO);
        return new SeilerThresholds(
                athleteId,
                estimatedLt1,
                estimatedLt2,
                null,
                null,
                java.time.LocalDate.now(),
                method,
                0.5 // Lower confidence for estimates
        );
    }

    /**
     * Classifies a power value into a zone.
     *
     * @param power the power to classify
     * @param lt1 the LT1 threshold
     * @param lt2 the LT2 threshold
     * @return the classified zone
     */
    public Zone classifyPower(Watts power, Watts lt1, Watts lt2) {
        if (power.value() <= lt1.value()) {
            return Zone.Z1;
        }
        if (power.value() <= lt2.value()) {
            return Zone.Z2;
        }
        return Zone.Z3;
    }
}
