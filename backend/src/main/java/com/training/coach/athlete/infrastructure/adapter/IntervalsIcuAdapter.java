package com.training.coach.athlete.infrastructure.adapter;

import com.fasterxml.jackson.databind.JsonNode;
import com.training.coach.athlete.application.port.out.FitnessPlatformPort;
import com.training.coach.athlete.application.port.out.FitnessPlatformPort.Activity;
import com.training.coach.athlete.application.port.out.FitnessPlatformPort.WellnessData;
import com.training.coach.shared.domain.unit.BeatsPerMinute;
import com.training.coach.shared.domain.unit.HeartRateVariability;
import com.training.coach.shared.domain.unit.Hours;
import com.training.coach.shared.domain.unit.Kilograms;
import com.training.coach.shared.domain.unit.Kilometers;
import com.training.coach.shared.domain.unit.Seconds;
import com.training.coach.shared.domain.unit.Watts;
import com.training.coach.shared.functional.Result;
import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

/**
 * Adapter for Intervals.icu fitness platform.
 */
@Component
@org.springframework.context.annotation.Profile("!test")
public class IntervalsIcuAdapter implements FitnessPlatformPort {

    private static final String CACHE_ACTIVITIES = "intervalsActivities";
    private static final String CACHE_WELLNESS = "intervalsWellness";
    private static final String CACHE_WELLNESS_RANGE = "intervalsWellnessRange";

    private final WebClient webClient;
    private final Retry retrySpec;

    public IntervalsIcuAdapter(
            @Value("${intervals.icu.api-key}") String apiKey,
            @Value("${intervals.icu.base-url:https://intervals.icu}") String baseUrl,
            @Value("${intervals.icu.retry.max-attempts:3}") int maxAttempts,
            @Value("${intervals.icu.retry.initial-delay:PT1S}") Duration initialDelay,
            @Value("${intervals.icu.retry.max-delay:PT5S}") Duration maxDelay) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "INTERVALS_ICU_API_KEY is not set. Provide it via environment variable or intervals.icu.api-key.");
        }
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeaders(headers -> {
                    headers.setBasicAuth("API_KEY", apiKey);
                    headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
                })
                .build();
        this.retrySpec = Retry.backoff(Math.max(1, maxAttempts - 1), initialDelay)
                .maxBackoff(maxDelay)
                .filter(IntervalsIcuAdapter::isRetryable)
                .onRetryExhaustedThrow((spec, signal) -> signal.failure());
    }

    @Override
    @Cacheable(cacheNames = CACHE_ACTIVITIES, unless = "#result != null && #result.isFailure()")
    public Result<List<Activity>> getActivities(String athleteId, LocalDate startDate, LocalDate endDate) {
        Result<List<Activity>> result = webClient
                .get()
                .uri(
                        "/api/v1/athlete/{athleteId}/activities?oldest={oldest}&newest={newest}",
                        athleteId,
                        startDate,
                        endDate)
                .exchangeToMono(response -> {
                    if (response.statusCode().is2xxSuccessful()) {
                        return response.bodyToMono(JsonNode.class)
                                .map(this::mapActivities)
                                .<Result<List<Activity>>>map(Result::success);
                    }
                    return response.createException().flatMap(Mono::error);
                })
                .retryWhen(retrySpec)
                .onErrorResume(
                        e -> Mono.just(Result.failure(buildFailure("activities", "Failed to fetch activities", e))))
                .block();
        return Objects.requireNonNullElseGet(
                result, () -> Result.failure(new RuntimeException("Failed to fetch activities")));
    }

    @Override
    @Cacheable(cacheNames = CACHE_WELLNESS, unless = "#result != null && #result.isFailure()")
    public Result<WellnessData> getWellnessData(String athleteId, LocalDate date) {
        Result<WellnessData> result = webClient
                .get()
                .uri("/api/v1/athlete/{athleteId}/wellness/{date}", athleteId, date)
                .exchangeToMono(response -> {
                    if (response.statusCode().is2xxSuccessful()) {
                        return response.bodyToMono(JsonNode.class)
                                .map(this::mapWellness)
                                .<Result<WellnessData>>map(Result::success);
                    }
                    return response.createException().flatMap(Mono::error);
                })
                .retryWhen(retrySpec)
                .onErrorResume(
                        e -> Mono.just(Result.failure(buildFailure("wellness", "Failed to fetch wellness data", e))))
                .block();
        return Objects.requireNonNullElseGet(
                result, () -> Result.failure(new RuntimeException("Failed to fetch wellness data")));
    }

    @Override
    @Cacheable(cacheNames = CACHE_WELLNESS_RANGE, unless = "#result != null && #result.isFailure()")
    public Result<List<WellnessData>> getWellnessDataRange(String athleteId, LocalDate startDate, LocalDate endDate) {
        Result<List<WellnessData>> result = webClient
                .get()
                .uri(
                        "/api/v1/athlete/{athleteId}/wellness?oldest={oldest}&newest={newest}",
                        athleteId,
                        startDate,
                        endDate)
                .exchangeToMono(response -> {
                    if (response.statusCode().is2xxSuccessful()) {
                        return response.bodyToMono(JsonNode.class)
                                .map(this::mapWellnessList)
                                .<Result<List<WellnessData>>>map(Result::success);
                    }
                    return response.createException().flatMap(Mono::error);
                })
                .retryWhen(retrySpec)
                .onErrorResume(e -> Mono.just(
                        Result.failure(buildFailure("wellness-range", "Failed to fetch wellness data range", e))))
                .block();

        return Objects.requireNonNullElseGet(
                result, () -> Result.failure(new RuntimeException("Failed to fetch wellness data range")));
    }

    private static RuntimeException buildFailure(String label, String defaultMessage, Throwable error) {
        if (error instanceof WebClientResponseException responseException) {
            String body = responseException.getResponseBodyAsString();
            HttpStatusCode status = responseException.getStatusCode();
            String message =
                    "Intervals.icu " + label + " error (" + status.value() + "): " + (body == null ? "" : body);
            return new RuntimeException(message, responseException);
        }
        return new RuntimeException(defaultMessage, error);
    }

    private static boolean isRetryable(Throwable error) {
        if (error instanceof WebClientRequestException) {
            return true;
        }
        if (error instanceof WebClientResponseException responseException) {
            HttpStatusCode status = responseException.getStatusCode();
            return status.value() == 429 || status.is5xxServerError();
        }
        return false;
    }

    private List<WellnessData> mapWellnessList(JsonNode root) {
        List<WellnessData> wellnessList = new ArrayList<>();
        if (root == null) {
            return wellnessList;
        }
        if (root.isArray()) {
            for (JsonNode node : root) {
                WellnessData wellness = mapWellnessItem(node);
                if (wellness != null) {
                    wellnessList.add(wellness);
                }
            }
        }
        return wellnessList;
    }

    private WellnessData mapWellnessItem(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        LocalDate date = parseDate(node, "date", "day", "created", "timestamp");
        double restingHeartRate = doubleValue(node, "restingHR", "resting_hr", "restingHeartRate", "rhr");
        double hrv = doubleValue(node, "hrv", "rmssd");
        double bodyWeightKg = doubleValue(node, "weight", "body_weight", "bodyWeight", "weight_kg");
        int sleepSecs = intValue(node, "sleepSecs", "sleep_secs");
        double sleepHours = sleepSecs > 0 ? sleepSecs / 3600.0 : 0.0;
        int sleepQuality = intValue(node, "sleepQuality", "sleep_quality");

        return new WellnessData(
                date == null ? LocalDate.now() : date,
                BeatsPerMinute.of(restingHeartRate),
                HeartRateVariability.of(hrv),
                Kilograms.of(bodyWeightKg),
                Hours.of(sleepHours),
                sleepQuality);
    }

    private List<Activity> mapActivities(JsonNode root) {
        List<Activity> activities = new ArrayList<>();
        if (root == null) {
            return activities;
        }
        if (root.isArray()) {
            for (JsonNode node : root) {
                Activity activity = mapActivity(node);
                if (activity != null) {
                    activities.add(activity);
                }
            }
        }
        return activities;
    }

    private Activity mapActivity(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        String id = firstNonEmpty(text(node, "id"), text(node, "activity_id"), text(node, "activityId"));
        LocalDate date = parseDate(node, "start_date_local", "start_date");
        String name = firstNonEmpty(text(node, "name"), text(node, "title"));
        int durationSeconds = intValue(node, "moving_time", "elapsed_time", "duration", "duration_seconds");
        double rawDistance = doubleValue(node, "distance", "icu_distance", "distance_m", "distanceMeters");
        double distanceKm = rawDistance > 500 ? rawDistance / 1000.0 : rawDistance;
        double averagePower =
                doubleValue(node, "average_watts", "avg_watts", "average_power", "avg_power", "icu_weighted_avg_watts");
        double averageHeartRate = doubleValue(node, "average_heartrate", "avg_heartrate", "avg_hr", "average_hr");
        double trainingStressScore = doubleValue(node, "tss", "icu_tss", "training_stress_score");
        double intensityFactor = doubleValue(node, "if", "intensity_factor", "icu_if");
        double normalizedPower = doubleValue(node, "np", "normalized_power", "icu_normalized_power");
        String type = firstNonEmpty(text(node, "type"), text(node, "sport_type"), text(node, "sportType"));

        return new Activity(
                id == null ? "" : id,
                date == null ? LocalDate.now() : date,
                name == null ? "" : name,
                Seconds.of(durationSeconds),
                Kilometers.of(distanceKm),
                Watts.of(averagePower),
                BeatsPerMinute.of(averageHeartRate),
                type == null ? "" : type,
                trainingStressScore > 0 ? trainingStressScore : null,
                intensityFactor > 0 ? intensityFactor : null,
                normalizedPower > 0 ? Watts.of(normalizedPower) : null);
    }

    private WellnessData mapWellness(JsonNode root) {
        JsonNode node = firstNode(root);
        if (node == null) {
            return new WellnessData(
                    LocalDate.now(),
                    BeatsPerMinute.of(0.0),
                    HeartRateVariability.of(0.0),
                    Kilograms.of(0.0),
                    Hours.of(0.0),
                    0);
        }
        LocalDate date = parseDate(node, "id", "date", "day", "created", "timestamp");
        double restingHeartRate = doubleValue(node, "restingHR", "resting_hr", "restingHeartRate", "rhr");
        double hrv = doubleValue(node, "hrv", "rmssd");
        double bodyWeightKg = doubleValue(node, "weight", "body_weight", "bodyWeight", "weight_kg");
        int sleepSecs = intValue(node, "sleepSecs", "sleep_secs");
        double sleepHours = sleepSecs > 0 ? sleepSecs / 3600.0 : 0.0;
        int sleepQuality = intValue(node, "sleepQuality", "sleep_quality");

        return new WellnessData(
                date == null ? LocalDate.now() : date,
                BeatsPerMinute.of(restingHeartRate),
                HeartRateVariability.of(hrv),
                Kilograms.of(bodyWeightKg),
                Hours.of(sleepHours),
                sleepQuality);
    }

    private JsonNode firstNode(JsonNode root) {
        if (root == null) {
            return null;
        }
        if (root.isArray()) {
            return root.isEmpty() ? null : root.get(0);
        }
        return root;
    }

    private String text(JsonNode node, String field) {
        if (node.hasNonNull(field)) {
            return node.get(field).asText();
        }
        return null;
    }

    private String firstNonEmpty(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private int intValue(JsonNode node, String... fields) {
        for (String field : fields) {
            if (node.hasNonNull(field)) {
                return node.get(field).asInt();
            }
        }
        return 0;
    }

    private double doubleValue(JsonNode node, String... fields) {
        for (String field : fields) {
            if (node.hasNonNull(field)) {
                return node.get(field).asDouble();
            }
        }
        return 0.0;
    }

    private LocalDate parseDate(JsonNode node, String... fields) {
        for (String field : fields) {
            if (node.hasNonNull(field)) {
                String value = node.get(field).asText();
                if (value == null || value.isBlank()) {
                    continue;
                }
                try {
                    if (value.contains("T")) {
                        return OffsetDateTime.parse(value).toLocalDate();
                    }
                    return LocalDate.parse(value);
                } catch (DateTimeParseException ignored) {
                    // try next field
                }
            }
        }
        return null;
    }
}
