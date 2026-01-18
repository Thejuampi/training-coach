package com.training.coach.common;

public record AuthTokens(String accessToken, String refreshToken, long expiresInSeconds) {
}