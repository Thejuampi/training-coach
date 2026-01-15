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
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Adapter for Intervals.icu fitness platform.
 */
@Component
public class IntervalsIcuAdapter implements FitnessPlatformPort {

    private final WebClient webClient;

    public IntervalsIcuAdapter(
            @Value("${intervals.icu.api-key}") String apiKey,
            @Value("${intervals.icu.base-url:https://intervals.icu}") String baseUrl) {
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
    }

    @Override
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
                    return response.bodyToMono(String.class)
                            .defaultIfEmpty("")
                            .<Result<List<Activity>>>map(
                                    body -> Result.failure(new RuntimeException("Intervals.icu activities error ("
                                            + response.statusCode().value()
                                            + "): "
                                            + body)));
                })
                .onErrorResume(e -> Mono.just(Result.failure(new RuntimeException("Failed to fetch activities", e))))
                .block();
        return Objects.requireNonNullElseGet(
                result, () -> Result.failure(new RuntimeException("Failed to fetch activities")));
    }

    @Override
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
                    return response.bodyToMono(String.class)
                            .defaultIfEmpty("")
                            .<Result<WellnessData>>map(
                                    body -> Result.failure(new RuntimeException("Intervals.icu wellness error ("
                                            + response.statusCode().value()
                                            + "): "
                                            + body)));
                })
                .onErrorResume(e -> Mono.just(Result.failure(new RuntimeException("Failed to fetch wellness data", e))))
                .block();
        return Objects.requireNonNullElseGet(
                result, () -> Result.failure(new RuntimeException("Failed to fetch wellness data")));
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
        String type = firstNonEmpty(text(node, "type"), text(node, "sport_type"), text(node, "sportType"));

        return new Activity(
                id == null ? "" : id,
                date == null ? LocalDate.now() : date,
                name == null ? "" : name,
                Seconds.of(durationSeconds),
                Kilometers.of(distanceKm),
                Watts.of(averagePower),
                BeatsPerMinute.of(averageHeartRate),
                type == null ? "" : type);
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
