package com.training.coach.athlete.domain.model;

import java.time.LocalDateTime;

public record Notification(String id, String athleteId, String message, LocalDateTime timestamp) {}