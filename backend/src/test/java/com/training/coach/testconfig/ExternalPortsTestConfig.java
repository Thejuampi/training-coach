package com.training.coach.testconfig;

import com.training.coach.athlete.application.port.out.FitnessPlatformPort;
import com.training.coach.testconfig.inmemory.TestFitnessPlatformPort;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

/**
 * Test configuration for external ports using in-memory adapters.
 *
 * <p>Use this config when tests need FitnessPlatformPort without calling external APIs.</p>
 */
@TestConfiguration
@Profile("test")
public class ExternalPortsTestConfig {

    @Bean
    public FitnessPlatformPort fitnessPlatformPort() {
        return new TestFitnessPlatformPort();
    }
}
