package com.training.coach.shared.util;

import java.time.Clock;
import java.time.Instant;

/**
 * Clock abstraction for testing purposes.
 * Use this in functional core instead of direct Clock.systemUTC() calls.
 */
public interface TimeProvider {

    /**
     * Default time provider using system UTC clock.
     */
    TimeProvider SYSTEM = new SystemTimeProvider();

    /**
     * Returns the current instant.
     */
    Instant now();

    /**
     * System time provider implementation.
     */
    record SystemTimeProvider() implements TimeProvider {
        @Override
        public Instant now() {
            return Clock.systemUTC().instant();
        }
    }

    /**
     * Fixed time provider for testing.
     */
    record FixedTimeProvider(Instant instant) implements TimeProvider {
        @Override
        public Instant now() {
            return instant;
        }
    }
}
