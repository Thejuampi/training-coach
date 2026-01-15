package com.training.coach.user.domain.model;

public record SystemUser(String id, String name, UserRole role, UserPreferences preferences) {}
