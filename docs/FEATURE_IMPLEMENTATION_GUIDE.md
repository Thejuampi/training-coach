# Feature Implementation Guide

## Overview

This guide documents the implementation approach for the remaining Cucumber scenarios in the training-coach project. It establishes consistent patterns, architectural principles, and workflow guidance for all contributors.

---

## 1. Implementation Status Summary

### ✅ Completed Features (53 Cucumber Scenarios Passing)

| Feature | Scenarios | Status |
|---------|-----------|--------|
| `admin.feature` | 10 | All implemented |
| `coach.feature` | 15 | 9 completed (UC1, UC3, UC6, UC7, F6, F8, F9) |
| `athlete.feature` | 13 | 4 completed (UC4, UC5, UC9 + smoke) |
| `auth.feature` | ~10 | Implemented |
| `plan-lifecycle.feature` | 4 | Not started |
| `compliance-progress.feature` | 3 | Not started |
| `seiler-intensity.feature` | 7 | Not started |
| `safety-and-guardrails.feature` | 5 | Not started |
| `zones-and-testing.feature` | 2 | Not started |
| `notifications.feature` | 3 | Not started |

### 🎯 Priority Order for Implementation

1. **Seiler Intensity Modeling** - Core training logic (polarized zones, LT1/LT2)
2. **Safety and Guardrails** - Critical athlete protection (block intensity when fatigue high)
3. **Zones and Testing** - FTP testing workflow
4. **Plan Lifecycle** - Draft → Publish → Revise → Archive
5. **Compliance and Progress** - Tracking metrics
6. **Notifications** - Reminders and alerts
7. **Remaining Coach/Athlete workflows** - F10-F16 scenarios

---

## 2. Architectural Principles

### Hexagonal Architecture (Ports & Adapters)

The project follows **Imperative Shell / Functional Core** pattern:

```
┌─────────────────────────────────────────────────────────────┐
│                    APPLICATION LAYER                        │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────┐  │
│  │ AthleteService  │  │  PlanService    │  │ SyncService │  │
│  └────────┬────────┘  └────────┬────────┘  └──────┬──────┘  │
└───────────┼────────────────────┼──────────────────┼─────────┘
            │                    │                  │
            ▼                    ▼                  ▼
┌─────────────────────────────────────────────────────────────┐
│                     DOMAIN LAYER                             │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────┐  │
│  │ Athlete Domain  │  │  TrainingPlan   │  │ Readiness   │  │
│  │ Model (record)  │  │  Domain Model   │  │ Calculator  │  │
│  └─────────────────┘  └─────────────────┘  └─────────────┘  │
│          ▲                    ▲                  ▲          │
└──────────┼────────────────────┼──────────────────┼──────────┘
           │                    │                  │
           │                    │                  │
┌──────────┴────────────────────┴──────────────────┴──────────┐
│                 PORTS (Application Contracts)                │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────┐  │
│  │FitnessPlatform  │  │  PlanGeneration │  │ AIAdvice    │  │
│  │Port             │  │  Port           │  │ Port        │  │
│  └─────────────────┘  └─────────────────┘  └─────────────┘  │
└──────────┬────────────────────┬──────────────────┬──────────┘
           │                    │                  │
           ▼                    ▼                  ▼
┌─────────────────────────────────────────────────────────────┐
│                INFRASTRUCTURE (Adapters)                     │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────┐  │
│  │Intervals.icu    │  │  InMemory       │  │ Claude/OpenAI│  │
│  │Adapter          │  │  Repository     │  │ Adapter     │  │
│  └─────────────────┘  └─────────────────┘  └─────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

### Key Rules

1. **Domain Layer** (`*/domain/model`)
   - Pure Java/Kotlin, no framework annotations
   - Immutable records where possible
   - Validation in compact constructors
   - No Spring dependencies

2. **Application Layer** (`*/application/service`)
   - Orchestrates domain logic
   - Stateless singleton services
   - Uses ports for external dependencies
   - Returns `Result<T>` for recoverable failures

3. **Infrastructure Layer** (`*/infrastructure`)
   - Adapters implement ports
   - IO operations (DB, network, file)
   - Framework-specific code

4. **Presentation Layer** (`*/presentation`)
   - Thin controllers
   - Map HTTP to application services
   - Handle errors via `ResponseStatusException`

---

## 3. Implementation Workflow

### Phase 1: Analyze Scenario Requirements

For each Gherkin scenario, extract:

```gherkin
Scenario: [Name]
  Given [preconditions]
  When [action]
  Then [assertions]
```

**Identify:**
- Domain entities involved
- Application services needed
- New ports/ports modifications
- Repository operations required
- Edge cases and invariants

### Phase 2: Design Domain Model

**Follow these rules:**

1. **Use Records for Domain Types**
   ```java
   // ✅ GOOD
   public record TrainingMetrics(
       double ftp,
       double fthr,
       Double vo2max,
       LocalDate effectiveDate
   ) {
       TrainingMetrics {
           if (ftp <= 0) throw new IllegalArgumentException("FTP must be positive");
       }
   }
   ```

2. **Use Value Objects for Units**
   ```java
   // ✅ GOOD
   public record Watts(double value) {
       public Watts { if (value < 0) throw new IllegalArgumentException(); }
   }
   ```

3. **Keep Domain Pure**
   ```java
   // ✅ GOOD - No Spring, no IO
   public record ZoneDistribution(
       double z1Percent,
       double z2Percent,
       double z3Percent
   ) {
       public boolean isPolarized() {
           return z1Percent >= 75 && z3Percent >= 15 && z2Percent <= 10;
       }
   }
   ```

4. **Factory Methods for Complex Creation**
   ```java
   // ✅ GOOD
   public record TrainingPlan(...) {
       public static TrainingPlan draft(Athlete athlete, LocalDate start, int weeks) {
           // ... validation and creation
       }
   }
   ```

### Phase 3: Implement Application Services

**Pattern:**
```kotlin
@Service
class TrainingPlanService(
    private val planRepository: PlanRepositoryPort,
    private val athleteRepository: AthleteRepositoryPort,
    private val planGenerator: PlanGenerationPort
) {
    fun generatePlan(request: PlanGenerationRequest): Result<TrainingPlan> {
        return try {
            val athlete = athleteRepository.findById(request.athleteId())
                .orElseThrow { IllegalArgumentException("Athlete not found") }

            val plan = planGenerator.generate(
                athlete = athlete,
                phase = request.phase(),
                startDate = request.startDate(),
                weeks = request.weeks()
            )

            Result.success(plan)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

### Phase 4: Add Cucumber Step Definitions

**Pattern (Kotlin):**
```kotlin
@ScenarioScope
class UseCaseSteps(
    private val athleteService: AthleteService,
    private val planService: TrainingPlanService,
    private val integrationService: IntegrationService
) {
    private var athletes = mutableMapOf<String, Athlete>()
    private var plans = mutableMapOf<String, TrainingPlan>()

    @Given("a saved athlete with FTP {double}")
    fun savedAthleteWithFtp(ftp: Double) {
        val athlete = Athlete(
            id = UUID.randomUUID().toString(),
            name = "Test Athlete",
            metrics = TrainingMetrics(ftp = ftp)
        )
        athleteService.save(athlete)
        athletes["current"] = athlete
    }

    @When("the coach generates a plan for phase {string}")
    fun generatePlan(phase: String) {
        val athlete = athletes["current"]!!
        val result = planService.generatePlan(
            PlanGenerationRequest(
                athleteId = athlete.id(),
                phase = Phase.valueOf(phase.uppercase()),
                startDate = LocalDate.now()
            )
        )
        assertThat(result.isSuccess).isTrue
        plans["current"] = result.value().orElseThrow()
    }

    @Then("the plan targets zone {string} at least {double} percent")
    fun planTargetsZone(zone: String, percent: Double) {
        val plan = plans["current"]!!
        val distribution = plan.zoneDistribution()
        when (zone.uppercase()) {
            "Z1" -> assertThat(distribution.z1Percent()).isGreaterThanOrEqualTo(percent)
            "Z2" -> assertThat(distribution.z2Percent()).isLessThanOrEqualTo(percent)
            "Z3" -> assertThat(distribution.z3Percent()).isGreaterThanOrEqualTo(percent)
        }
    }
}
```

### Phase 5: Run Tests and Verify

```bash
# Run only Cucumber tests
mvnw -pl backend -Dtest=CucumberTest test

# Run full test suite
mvnw -pl backend test

# Verify code quality
mvnw -pl backend spotless:check
mvnw -pl backend spotbugs:check
mvnw -pl backend checkstyle:check
```

---

## 4. Feature-Specific Implementation Guidance

### 4.1 Seiler Intensity Modeling (`seiler-intensity.feature`)

**Core Concepts:**
- **LT1 (Lactate Threshold 1)**: ~75% of FTP, boundary Z1/Z2
- **LT2 (Lactate Threshold 2)**: ~90% of FTP, boundary Z2/Z3
- **3-Zone Model**: Z1 (polarized low), Z2 (threshold), Z3 (high intensity)
- **Polarized Distribution**: ~80% Z1, ~10% Z2, ~10% Z3
- **Z2 Creep**: Warning when Z2 exceeds 20%

**Required Domain Models:**
```java
// IntensityZones.java
public record IntensityZones(
    Watts lt1,
    Watts lt2,
    Watts z1UpperBound,      // ~LT1
    Watts z2UpperBound,      // ~LT2
    Watts z3UpperBound       // Max sustainable
) {
    public Zone classify(Watts power) {
        if (power.value() <= z1UpperBound.value()) return Zone.Z1;
        if (power.value() <= z2UpperBound.value()) return Zone.Z2;
        return Zone.Z3;
    }
}

// PolarizedDistribution.java
public record PolarizedDistribution(
    double z1Percent,
    double z2Percent,
    double z3Percent
) {
    public static PolarizedDistribution fromWorkouts(List<Workout> workouts) {
        // Calculate weighted distribution
    }

    public boolean hasZ2Creep() {
        return z2Percent > 20.0;
    }
}
```

**Services to Enhance/Create:**
- `IntensityZoneService` - Calculate zones from LT1/LT2
- `SessionClassifierService` - Classify workouts as polarized/tempo/threshold
- `DistributionAnalyzer` - Detect Z2 creep

**Key Scenarios:**
1. ✅ Establish LT1/LT2 via test protocol
2. ✅ Classify session (polarized vs tempo vs threshold)
3. ✅ FATMAX prescription (Z1 below LT1)
4. ✅ Detect Z2 creep
5. ✅ Plan generation uses polarized constraints
6. ✅ VO2-optimal vs Sprint classification
7. ✅ Adjustment based on readiness

---

### 4.2 Safety and Guardrails (`safety-and-guardrails.feature`)

**Core Rules:**
- **SG-FATIGUE-001**: Block INTERVALS, VO2_MAX, THRESHOLD, SPRINT when fatigue >= 8 AND soreness >= 8
- **SG-LOAD-001**: Cap weekly load increase at 15%
- **SG-RECOVERY-001**: Minimum 2 recovery days between high-intensity sessions
- **SG-AI-001**: Filter AI suggestions by readiness (block if readiness < 40)
- **SG-OVERRIDE-001**: Admin can override with justification and audit log
- **SG-CONFIG-001**: Configurable thresholds with versioning

**Required Domain Models:**
```java
// GuardrailRule.java
public record GuardrailRule(
    String ruleId,
    GuardrailType type,
    String description,
    Predicate<AthleteContext> condition,
    String blockingReason,
    List<String> suggestedAlternatives
) {}

// SafetyDecision.java
public sealed interface SafetyDecision {
    record Approved(List<String> suggestions) implements SafetyDecision {}
    record Blocked(
        String ruleId,
        String reason,
        List<String> alternatives,
        List<String> notifications
    ) implements SafetyDecision {}
}
```

**Services to Create:**
- `SafetyGuardrailService` - Evaluate rules and return decisions
- `LoadRampValidator` - Check weekly load progression
- `RecoveryEnforcer` - Track days between high-intensity sessions
- `AuditLogService` - Log guardrail events

**Key Scenarios:**
1. Block intensity when fatigue flags present (SG-FATIGUE-001)
2. Cap week-over-week load ramp (SG-LOAD-001)
3. Enforce recovery days (SG-RECOVERY-001)
4. AI suggestions obey guardrails (SG-AI-001)
5. Admin override with justification (SG-OVERRIDE-001)
6. Configure guardrail thresholds (SG-CONFIG-001)

---

### 4.3 Zones and Testing (`zones-and-testing.feature`)

**Core Concepts:**
- **FTP Test**: Ramp test to determine functional threshold power
- **Zone Recalculation**: Automatic zone update after new FTP
- **Prescription Bands**: Include method + confidence (LAB_LACTATE, FIELD_TEST, ESTIMATED)

**Required Domain Models:**
```java
// FtpTestResult.java
public record FtpTestResult(
    Watts ftp,
    LocalDate testDate,
    TestMethod method,
    double confidencePercent
) {
    public enum TestMethod {
        LAB_LACTATE,
        FIELD_RAMP,
        FIELD_20MIN,
        ESTIMATED
    }
}

// PrescriptionBand.java
public record PrescriptionBand(
    Zone zone,
    WattsRange targetRange,
    String method,
    double confidence
) {}
```

**Services to Create/Enhance:**
- `FtpTestService` - Process FTP test results
- `ZoneCalculationService` - Recalculate zones from new metrics

**Key Scenarios:**
1. FTP test updates zones with method + confidence
2. Updated FTP affects future prescriptions only (not historical)

---

### 4.4 Plan Lifecycle (`plan-lifecycle.feature`)

**States:**
- **DRAFT**: Initial generation, not visible to athlete
- **PUBLISHED**: Visible to athlete, active
- **ARCHIVED**: Past plans, accessible for history

**Required Domain Models:**
```java
// PlanVersion.java
public record PlanVersion(
    int versionNumber,
    TrainingPlan plan,
    Instant createdAt,
    String createdBy,
    String changeNote
) {}

// PlanStatus.java
public enum PlanStatus {
    DRAFT,
    PUBLISHED,
    ARCHIVED
}

// TrainingPlan.java
public record TrainingPlan(
    String id,
    String athleteId,
    Phase phase,
    LocalDate startDate,
    LocalDate endDate,
    PlanStatus status,
    List<Workout> workouts,
    int currentVersion
) {
    public List<PlanVersion> versionHistory() { /* ... */ }
}
```

**Services to Create:**
- `PlanLifecycleService` - Manage draft → publish → revise → archive
- `PlanVersionService` - Track plan history

**Key Scenarios:**
1. Draft generation produces a draft plan
2. Publishing makes plan visible to athlete
3. Revision creates new version (keep history)
4. Archive plan at end of cycle

---

### 4.5 Compliance and Progress (`compliance-progress.feature`)

**Metrics:**
- **Completion Rate**: Planned vs completed workouts
- **Key Session Completion**: Critical workouts missed
- **Zone Distribution Adherence**: 80/20 target
- **Z2 Creep Flag**: Warning when Z2 > 20%
- **Load Trend**: CTL/ATL progression
- **Streaks**: Consecutive completion days

**Required Domain Models:**
```java
// ComplianceMetrics.java
public record ComplianceMetrics(
    double completionPercent,
    int keySessionsCompleted,
    int keySessionsTotal,
    PolarizedDistribution actualDistribution,
    PolarizedDistribution targetDistribution,
    List<String> flags  // e.g., "Z2_CREEP"
) {}

// ProgressSummary.java
public record ProgressSummary(
    List<WeeklyVolume> volumeTrend,
    List<WeeklyLoad> loadTrend,
    List<Integer> completionStreaks
) {}
```

**Services to Create:**
- `ComplianceService` - Calculate completion and adherence
- `ProgressAnalysisService` - Analyze trends and streaks

**Key Scenarios:**
1. Weekly compliance includes key sessions and zone distribution
2. Ad-hoc activity classification (unplanned load)
3. Progress summary with trends and streaks

---

### 4.6 Notifications (`notifications.feature`)

**Notification Types:**
- **Workout Reminder**: Daily, for tomorrow's workout
- **Coach Alert**: Missed key session, fatigue warning
- **Fatigue Warning**: Athlete + coach notification

**Required Domain Models:**
```java
// Notification.java
public record Notification(
    String id,
    String userId,
    NotificationType type,
    String title,
    String message,
    Instant createdAt,
    boolean read
) {
    public enum NotificationType {
        WORKOUT_REMINDER,
        KEY_SESSION_MISSED,
        FATIGUE_WARNING,
        PLAN_CHANGE,
        WELLNESS_REMINDER
    }
}
```

**Services to Create:**
- `NotificationService` - Create and dispatch notifications
- `DailyNotificationJob` - Scheduled job for reminders

**Key Scenarios:**
1. Daily workout reminder sent
2. Missed key session triggers coach alert
3. Low readiness streak triggers fatigue warning

---

## 5. Common Implementation Patterns

### 5.1 Repository Pattern (In-Memory for Tests)

```kotlin
// InMemoryRepository.kt
@Component
@ScenarioScope
class InMemoryRepository<T : Entity>(
    private val store: MutableMap<String, T> = ConcurrentHashMap()
) {
    fun save(entity: T): T {
        store[entity.id()] = entity
        return entity
    }

    fun findById(id: String): T? = store[id]

    fun findAll(): List<T> = store.values.toList()

    fun delete(id: String): Boolean = store.remove(id) != null

    fun clear() = store.clear()
}
```

### 5.2 Result Pattern for Operations

```java
// Using Result<T> for recoverable failures
public record Result<T>(
    T value,
    Throwable error,
    boolean isSuccess
) {
    public static <T> Result<T> success(T value) {
        return new Result<>(value, null, true);
    }

    public static <T> Result<T> failure(Throwable error) {
        return new Result<>(null, error, false);
    }

    public T orElseThrow() {
        if (isSuccess) return value;
        throw new RuntimeException(error);
    }
}
```

### 5.3 Step Definition Best Practices

```kotlin
// ✅ GOOD: Clear, focused steps
@Given("a saved athlete with LT1 watts {double} and LT2 watts {double}")
fun savedAthleteWithLt1Lt2(lt1: Double, lt2: Double) {
    val athlete = Athlete(
        id = UUID.randomUUID().toString(),
        name = "Test Athlete",
        metrics = TrainingMetrics(ftp = (lt1 * 1.2).roundToInt().toDouble())
    )
    athleteService.updateZones(athlete.id, Watts(lt1), Watts(lt2))
    athletes["current"] = athlete
}

// ✅ GOOD: Reuse existing steps
@Given("a published plan exists for a saved athlete")
fun publishedPlanExists() {
    savedAthlete()
    athleteHasAvailability("MONDAY,WEDNESDAY,FRIDAY", 8.0, "base")
    coachGeneratesPlan("base", "2026-01-01", 4)
    coachPublishesPlan()
}

// ✅ GOOD: Descriptive assertions
@Then("the plan targets zone {string} at least {double} percent")
fun planTargetsZone(zone: String, percent: Double) {
    val plan = plans["current"]!!
    val distribution = plan.zoneDistribution()

    when (zone.uppercase()) {
        "Z1" -> assertThat(distribution.z1Percent())
            .as("Z1 should be at least ${percent}%")
            .isGreaterThanOrEqualTo(percent)
        "Z2" -> assertThat(distribution.z2Percent())
            .as("Z2 should be at most ${percent}%")
            .isLessThanOrEqualTo(percent)
        "Z3" -> assertThat(distribution.z3Percent())
            .as("Z3 should be around ${percent}%")
            .isGreaterThanOrEqualTo(percent)
    }
}
```

---

## 6. Code Style Rules

### Imports
```java
// ✅ GOOD
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

// ❌ BAD
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
```

### Record Definitions
```java
// ✅ GOOD - Compact constructor with validation
public record TrainingMetrics(
    double ftp,
    double fthr,
    Double vo2max,
    LocalDate effectiveDate
) {
    public TrainingMetrics {
        if (ftp <= 0) {
            throw new IllegalArgumentException("FTP must be positive");
        }
        if (fthr <= 0) {
            throw new IllegalArgumentException("FTHR must be positive");
        }
    }
}

// ❌ BAD - Validation in constructor body
public record TrainingMetrics(double ftp, double fthr) {
    public TrainingMetrics {
        if (ftp <= 0) {
            throw new IllegalArgumentException();
        }
    }
}
```

### Naming Conventions
| Element | Convention | Example |
|---------|------------|---------|
| Domain records | `PascalCase` | `TrainingMetrics`, `IntensityZones` |
| Services | `PascalCase` + `Service` | `PlanGenerationService` |
| Methods | `camelCase` | `generatePlan()`, `calculateZones()` |
| Constants | `UPPER_SNAKE_CASE` | `DEFAULT_WEEKLY_RAMP_CAP` |
| Step definitions | `camelCase` | `@Given("a saved athlete")` |

---

## 7. Testing Strategy

### Test Pyramid for Each Feature

```
        /\
       /  \      Acceptance Tests (Cucumber)
      /____\     1-2 scenarios per feature
     /      \
    /        \   Unit Tests (JUnit 5)
   /__________\  5-10 tests per service/class
  /            \
 /              \ Integration Tests (Spring)
/________________\ 2-3 tests per adapter/port
```

### Cucumber Test Guidelines

1. **Use `@ScenarioScope` for test state**
   ```kotlin
   @ScenarioScope
   class UseCaseSteps(...) {
       // State is isolated per scenario
       private val athletes = mutableMapOf<String, Athlete>()
   }
   ```

2. **Prefer in-memory repositories**
   ```kotlin
   // ✅ GOOD - Fast, isolated
   @Bean
   @ScenarioScope
   fun athleteRepository(): AthleteRepositoryPort {
       return InMemoryAthleteRepository()
   }
   ```

3. **Avoid external IO in tests**
   ```kotlin
   // ✅ GOOD - Mock the port
   @Bean
   @ScenarioScope
   fun fitnessPlatformPort(): FitnessPlatformPort {
       return TestFitnessPlatformPort()  // In-memory implementation
   }
   ```

4. **Use AssertJ for assertions**
   ```kotlin
   assertThat(result.isSuccess).isTrue
   assertThat(athlete.ftp()).isEqualTo(240.0)
   assertThat(plan.zoneDistribution().z1Percent()).isGreaterThanOrEqualTo(75.0)
   ```

### Running Tests

```bash
# Run all Cucumber scenarios
mvnw -pl backend -Dtest=CucumberTest test

# Run a specific feature
mvnw -pl backend -Dtest=CucumberTest test -Dcucumber.filter.tags="@smoke"

# Run a specific scenario
mvnw -pl backend -Dtest=CucumberTest test -Dcucumber.filter.name="Establish LT1 and LT2"

# Run with coverage
mvnw -pl backend test jacoco:report

# Code quality checks
mvnw -pl backend spotless:check
mvnw -pl backend spotbugs:check
mvnw -pl backend checkstyle:check
```

---

## 8. File Locations by Layer

### Domain Layer
```
backend/src/main/java/com/training/coach/
├── athlete/domain/model/
│   ├── Athlete.java
│   ├── AthleteProfile.java
│   ├── TrainingMetrics.java
│   ├── TrainingPreferences.java
│   └── ReadinessSnapshot.java
├── trainingplan/domain/model/
│   ├── TrainingPlan.java
│   ├── Workout.java
│   ├── Phase.java
│   └── CompletionStatus.java
├── analysis/domain/model/
│   ├── ComplianceMetrics.java
│   ├── ProgressSummary.java
│   └── LoadTrend.java
└── shared/domain/
    ├── unit/
    │   ├── Watts.java
    │   ├── HeartRate.java
    │   └── Duration.java
    └── model/
        ├── Result.java
        └── GuardrailRule.java
```

### Application Layer
```
backend/src/main/java/com/training/coach/
├── athlete/application/service/
│   └── AthleteService.java
├── trainingplan/application/service/
│   ├── TrainingPlanService.java
│   └── PlanGenerationService.java
├── analysis/application/service/
│   ├── ComplianceService.java
│   ├── ReadinessService.java
│   └── AdjustmentService.java
└── safety/application/service/
    ├── SafetyGuardrailService.java
    └── AuditLogService.java
```

### Ports (Application Contracts)
```
backend/src/main/java/com/training/coach/
└── application/port/
    ├── fitness/
    │   └── FitnessPlatformPort.java
    ├── persistence/
    │   ├── AthleteRepositoryPort.java
    │   ├── PlanRepositoryPort.java
    │   └── WorkoutRepositoryPort.java
    └── ai/
        └── AIAdvicePort.java
```

### Infrastructure (Adapters)
```
backend/src/main/java/com/training/coach/
├── athlete/infrastructure/
│   ├── persistence/
│   │   └── AthleteRepositoryAdapter.java
│   └── adapter/
│       └── IntervalsIcuAdapter.java
├── integration/infrastructure/
│   └── adapter/
│       └── IntervalsIcuSyncAdapter.java
└── ai/infrastructure/
    └── adapter/
        └── ClaudeAdapter.java
```

### Step Definitions (Tests)
```
backend/src/test/kotlin/com/training/coach/acceptance/
├── UseCaseSteps.kt          # Main step definitions
├── CucumberSpringConfiguration.kt  # Spring config for tests
├── TestFitnessPlatformPort.kt      # In-memory port implementation
├── InMemoryAthleteRepository.kt
├── InMemoryPlanRepository.kt
└── InMemoryWorkoutRepository.kt
```

---

## 9. Checklist for New Feature Implementation

### Before Writing Code
- [ ] Read and understand all scenarios in the feature file
- [ ] Identify domain entities and their relationships
- [ ] Determine required ports and adapters
- [ ] Review existing similar implementations for patterns
- [ ] Create domain model records (pure Java/Kotlin)
- [ ] Create or update application services
- [ ] Create Cucumber step definitions

### During Implementation
- [ ] Follow hexagonal architecture boundaries
- [ ] Use immutable records for domain types
- [ ] Validate invariants in compact constructors
- [ ] Use `Result<T>` for recoverable failures
- [ ] Document rule IDs for safety/guardrails
- [ ] Include method and confidence in prescriptions

### Testing
- [ ] Implement all Cucumber scenarios
- [ ] Add unit tests for domain logic
- [ ] Verify tests pass: `mvnw -pl backend test`
- [ ] Run code quality: `mvnw spotless:check`
- [ ] Check coverage: `mvnw jacoco:report`

### Before Commit
- [ ] Run full verification: `mvnw clean verify`
- [ ] Ensure all Cucumber scenarios pass
- [ ] No new Spotless violations
- [ ] No new SpotBugs/Checkstyle warnings
- [ ] Commit follows conventional format

---

## 10. Quick Reference Commands

```bash
# Build and test
mvnw.cmd clean verify                           # Full build
mvnw.cmd -pl backend test                       # Run tests
mvnw.cmd -pl backend -Dtest=CucumberTest test   # Cucumber only

# Code quality
mvnw.cmd spotless:check                         # Check formatting
mvnw.cmd spotless:apply                         # Fix formatting
mvnw.cmd spotbugs:check                         # Bug checks
mvnw.cmd checkstyle:check                       # Style checks

# Coverage
mvnw.cmd test jacoco:report                     # Generate report
mvnw.cmd test jacoco:report jacoco:check        # Enforce thresholds

# Run application
mvnw.cmd -pl backend spring-boot:run -Dspring-boot.run.profiles=dev
```

---

## 11. Next Steps

### Immediate Actions

1. **Start with Seiler Intensity Modeling**
   - Create `IntensityZones` record in domain
   - Create `ZoneCalculationService` in application
   - Add step definitions in `UseCaseSteps.kt`
   - Implement 7 scenarios

2. **Parallel Track: Safety Guardrails**
   - Create `GuardrailRule` and `SafetyDecision` records
   - Create `SafetyGuardrailService`
   - Add step definitions
   - Implement 5 scenarios

3. **Validate Progress**
   - Run `mvnw.cmd -pl backend -Dtest=CucumberTest test`
   - Verify 53 → 60+ scenarios passing
   - Check no regressions in existing tests

### Documentation Updates

- Update `docs/ARCHITECTURE.md` with new domain models
- Document new services in code comments
- Add `@see` references between related components

---

## 12. Common Pitfalls to Avoid

### ❌ Domain Layer Pollution
```java
// ❌ BAD - Domain depends on Spring
@Entity
public record Athlete(...) {
    @Id private String id;  // JPA annotation in domain!
}

// ✅ GOOD - Pure domain
public record Athlete(
    String id,
    String name,
    TrainingMetrics metrics
) {}
```

### ❌ Business Logic in Controllers
```java
// ❌ BAD - Controller contains logic
@PostMapping("/plan")
fun generatePlan(@RequestBody request: PlanRequest): ResponseEntity<*> {
    // Complex logic here!
    val zones = calculateZones(request.ftp())  // ❌
    val distribution = polarize(zones)         // ❌
    val workouts = generate(zones, distribution) // ❌
    return ok(workouts)
}

// ✅ GOOD - Controller delegates to service
@PostMapping("/plan")
fun generatePlan(@RequestBody request: PlanRequest): ResponseEntity<TrainingPlan> {
    val result = planService.generatePlan(request.toCommand())
    return result.fold(
        onSuccess = { ok(it) },
        onFailure = { badRequest(it.message) }
    )
}
```

### ❌ Mutable Domain Objects
```java
// ❌ BAD - Mutable state
public class Athlete {
    private double ftp;
    public void setFtp(double ftp) { this.ftp = ftp; }
}

// ✅ GOOD - Immutable record
public record Athlete(
    String id,
    double ftp,
    LocalDate ftpEffectiveDate
) {
    public Athlete withFtp(double newFtp) {
        return new Athlete(id, newFtp, LocalDate.now());
    }
}
```

### ❌ Ignoring Error Handling
```java
// ❌ BAD - Ignoring potential failures
public void saveAthlete(Athlete athlete) {
    repository.save(athlete);  // What if this fails?
}

// ✅ GOOD - Using Result
public Result<Athlete> saveAthlete(Athlete athlete) {
    return Result.tryCatch(() -> repository.save(athlete))
        .mapError(e -> new StorageException("Failed to save", e));
}
```

---

## 13. References

- **Architecture**: `docs/ARCHITECTURE.md`
- **Agent Guidelines**: `AGENTS.md`
- **Cucumber Best Practices**: `backend/src/test/kotlin/com/training/coach/acceptance/UseCaseSteps.kt`
- **Existing Implementations**: Review `IntegrationService.java` for service patterns
- **OpenAPI Spec**: `api/openapi.json` (after generation)

---

*Generated: January 2026 | Training Coach BDD Implementation Guide*
