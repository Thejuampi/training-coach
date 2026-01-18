package com.training.coach.tui.dto;

import java.time.Instant;

/**
 * Immutable representation of a system user.
 */
public record SystemUser(
        String id,
        String name,
        UserRole role,
        UserPreferences preferences,
        String username,
        boolean enabled,
        Instant createdAt,
        Instant lastLoginAt) {

    public SystemUser withLastLogin(Instant lastLogin) {
        return new SystemUser(id, name, role, preferences, username, enabled, createdAt, lastLogin);
    }
}