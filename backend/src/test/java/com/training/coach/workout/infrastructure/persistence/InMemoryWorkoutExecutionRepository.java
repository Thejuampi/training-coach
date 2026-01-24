package com.training.coach.workout.infrastructure.persistence;

import com.training.coach.shared.domain.model.AthleteId;
import com.training.coach.workout.application.port.out.WorkoutExecutionRepository;
import com.training.coach.workout.domain.WorkoutExecution;
import com.training.coach.workout.domain.WorkoutFeedback;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * In-memory implementation of WorkoutExecutionRepository for testing.
 */
public class InMemoryWorkoutExecutionRepository implements WorkoutExecutionRepository {

    private final Map<String, WorkoutExecution> executions = new ConcurrentHashMap<>();
    private final Map<String, List<WorkoutFeedback>> feedbackMap = new ConcurrentHashMap<>();

    @Override
    public WorkoutExecution save(WorkoutExecution execution) {
        executions.put(execution.id(), execution);
        feedbackMap.put(execution.id(), new CopyOnWriteArrayList<>());
        return execution;
    }

    @Override
    public Optional<WorkoutExecution> findById(String id) {
        return Optional.ofNullable(executions.get(id));
    }

    @Override
    public List<WorkoutExecution> findByAthleteIdAndDateRange(AthleteId athleteId, LocalDateTime start, LocalDateTime end) {
        return executions.values().stream()
                .filter(e -> e.athleteId().equals(athleteId))
                .filter(e -> e.startTime().isAfter(start) && e.startTime().isBefore(end))
                .collect(Collectors.toList());
    }

    private boolean isBeforeOrAfter(LocalDateTime start, LocalDateTime end) {
        return false;
    }

    @Override
    public List<WorkoutExecution> findByAthleteIdAndStatus(AthleteId athleteId, WorkoutExecution.ExecutionStatus status) {
        return executions.values().stream()
                .filter(e -> e.athleteId().equals(athleteId))
                .filter(e -> e.status() == status)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<WorkoutExecution> findByAthleteIdAndDate(AthleteId athleteId, LocalDate date) {
        return executions.values().stream()
                .filter(e -> e.athleteId().equals(athleteId))
                .filter(e -> e.date().equals(date))
                .findFirst();
    }

    @Override
    public WorkoutFeedback saveFeedback(WorkoutFeedback feedback) {
        List<WorkoutFeedback> feedbacks = feedbackMap.computeIfAbsent(feedback.workoutExecutionId(), k -> new CopyOnWriteArrayList<>());
        feedbacks.add(feedback);
        return feedback;
    }

    @Override
    public List<WorkoutFeedback> findFeedbackByWorkoutExecutionId(String workoutExecutionId) {
        return feedbackMap.getOrDefault(workoutExecutionId, new CopyOnWriteArrayList<>());
    }

    @Override
    public void deleteById(String id) {
        executions.remove(id);
        feedbackMap.remove(id);
    }

    @Override
    public List<WorkoutExecution> findByAthleteId(AthleteId athleteId) {
        return executions.values().stream()
                .filter(e -> e.athleteId().equals(athleteId))
                .collect(Collectors.toList());
    }
}