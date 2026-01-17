package com.training.coach.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "training.security.jwt")
public record JwtProperties(
        @DefaultValue("training-coach") String issuer,
        @DefaultValue("training-coach-api") String audience,
        @DefaultValue("600") long accessTokenTtlSeconds,
        @DefaultValue("14") long refreshTokenTtlDays) {
    public static JwtProperties defaults() {
        return new JwtProperties("training-coach", "training-coach-api", 600, 14);
    }
}
