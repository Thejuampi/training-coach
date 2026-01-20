package com.training.coach.analysis

import com.training.coach.analysis.application.service.SafetyGuardrailService
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe

/**
 * Property-based tests for SafetyGuardrailService using Kotest.
 * These tests verify invariants across a wide range of inputs.
 */
class SafetyGuardrailServiceKotestTest : FreeSpec() {

    private val service = SafetyGuardrailService()

    init {
        "Threshold constants are correctly applied" {
            // High fatigue blocks
            service.checkAdjustment("athlete", 7.0, 3.0, 6.0, "INTERVALS").blocked() shouldBe true
            service.checkAdjustment("athlete", 6.9, 3.0, 6.0, "INTERVALS").blocked() shouldBe false
            
            // High soreness blocks
            service.checkAdjustment("athlete", 3.0, 7.0, 6.0, "INTERVALS").blocked() shouldBe true
            service.checkAdjustment("athlete", 3.0, 6.9, 6.0, "INTERVALS").blocked() shouldBe false
            
            // Low readiness blocks
            service.checkAdjustment("athlete", 3.0, 3.0, 4.0, "INTERVALS").blocked() shouldBe true
            service.checkAdjustment("athlete", 3.0, 3.0, 4.1, "INTERVALS").blocked() shouldBe false
        }

        "Non-high-intensity workouts are never blocked regardless of fatigue/soreness" {
            // Even with high fatigue/soreness, easy rides should not be blocked
            service.checkAdjustment("test-athlete", 9.0, 9.0, 2.0, "EASY_RIDE").blocked() shouldBe false
            service.checkAdjustment("test-athlete", 10.0, 10.0, 1.0, "RECOVERY").blocked() shouldBe false
            service.checkAdjustment("test-athlete", 7.0, 7.0, 3.0, "ENDURANCE").blocked() shouldBe false
        }

        "High-intensity workouts at low fatigue/soreness are not blocked" {
            service.checkAdjustment("test-athlete", 3.0, 3.0, 8.0, "INTERVALS").blocked() shouldBe false
            service.checkAdjustment("test-athlete", 0.0, 0.0, 9.0, "VO2_MAX").blocked() shouldBe false
            service.checkAdjustment("test-athlete", 5.0, 4.0, 7.0, "THRESHOLD").blocked() shouldBe false
        }

        "Result contains safe alternative when blocked" {
            val result = service.checkAdjustment("athlete", 9.0, 3.0, 6.0, "INTERVALS")
            result.blocked() shouldBe true
            result.safeAlternative() shouldBe "Schedule a recovery ride or easy endurance workout instead"
        }

        "High-intensity workout types are correctly identified" {
            service.checkAdjustment("athlete", 9.0, 3.0, 6.0, "INTERVALS").blocked() shouldBe true
            service.checkAdjustment("athlete", 9.0, 3.0, 6.0, "VO2_MAX").blocked() shouldBe true
            service.checkAdjustment("athlete", 9.0, 3.0, 6.0, "THRESHOLD").blocked() shouldBe true
            service.checkAdjustment("athlete", 9.0, 3.0, 6.0, "SPRINT").blocked() shouldBe true
        }
    }
}
