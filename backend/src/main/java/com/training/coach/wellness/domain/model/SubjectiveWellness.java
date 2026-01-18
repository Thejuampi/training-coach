package com.training.coach.wellness.domain.model;

public record SubjectiveWellness(
        int fatigueScore,
        int stressScore,
        int sleepQualityScore,
        int motivationScore,
        int muscleSorenessScore,
        String notes) {
    public SubjectiveWellness {
        validateScore(fatigueScore, "Fatigue");
        validateScore(stressScore, "Stress");
        validateScore(sleepQualityScore, "Sleep quality");
        validateScore(motivationScore, "Motivation");
        validateScore(muscleSorenessScore, "Muscle soreness");
    }

    private static void validateScore(int score, String fieldName) {
        if (score < 1 || score > 10) {
            throw new IllegalArgumentException(fieldName + " score must be between 1 and 10");
        }
    }

    public static SubjectiveWellness create(int fatigue, int stress, int sleepQuality, int motivation, int soreness) {
        return new SubjectiveWellness(fatigue, stress, sleepQuality, motivation, soreness, null);
    }

    public static SubjectiveWellness withNotes(
            int fatigue, int stress, int sleepQuality, int motivation, int soreness, String notes) {
        return new SubjectiveWellness(fatigue, stress, sleepQuality, motivation, soreness, notes);
    }
}
