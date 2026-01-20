package com.training.coach.analysis.domain.model;

/**
 * Classification of workout intensity based on physiological targets.
 */
public enum IntensityClassification {
    /** VO2-optimal: High-intensity intervals at ~110-120% FTP */
    VO2_OPTIMAL,

    /** Sprint: Very high intensity, short duration at >130% FTP */
    SPRINT,

    /** Threshold: Moderate-high intensity at LT2 */
    THRESHOLD,

    /** Tempo: Moderate intensity, between LT1 and LT2 */
    TEMPO,

    /** Endurance: Low intensity, below LT1 */
    ENDURANCE,

    /** Recovery: Very low intensity, well below LT1 */
    RECOVERY,

    /** FATMAX: Optimal fat oxidation zone, below LT1 */
    FATMAX
}
