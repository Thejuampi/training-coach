package com.training.coach.testconfig.inmemory;

import com.training.coach.privacy.application.port.out.DataDeletionRequestRepository;
import com.training.coach.privacy.domain.model.DataDeletionRequest;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory DataDeletionRequestRepository for fast tests.
 */
public class InMemoryDataDeletionRequestRepository implements DataDeletionRequestRepository {
    private final ConcurrentHashMap<String, DataDeletionRequest> requests = new ConcurrentHashMap<>();

    @Override
    public DataDeletionRequest save(DataDeletionRequest request) {
        requests.put(request.id(), request);
        return request;
    }

    @Override
    public Optional<DataDeletionRequest> findById(String requestId) {
        return Optional.ofNullable(requests.get(requestId));
    }

    @Override
    public List<DataDeletionRequest> findByAthleteId(String athleteId) {
        return requests.values().stream()
                .filter(r -> r.athleteId().equals(athleteId))
                .collect(Collectors.toList());
    }

    @Override
    public List<DataDeletionRequest> findPending() {
        return requests.values().stream()
                .filter(DataDeletionRequest::isPending)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(String requestId) {
        requests.remove(requestId);
    }

    /**
     * Clear all requests for testing purposes.
     */
    public void clearAll() {
        requests.clear();
    }
}
