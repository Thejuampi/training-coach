package com.training.coach.testconfig;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * Test configuration for WebFlux controllers with permissive security.
 *
 * <p>Use this config for controller tests that should bypass authentication.</p>
 */
@TestConfiguration
@Profile("test")
public class WebTestConfig {

    @Bean
    @Order(0)
    SecurityWebFilterChain testSecurityWebFilterChain(ServerHttpSecurity http) {
        return http.csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchange -> exchange.anyExchange().permitAll())
                .build();
    }
}
