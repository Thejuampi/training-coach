package com.training.coach.athlete.domain.model;

import java.time.LocalDate;

public record Event(String id, String athleteId, String name, LocalDate date, String priority) {}