package com.training.coach.testconfig.inmemory;

import com.training.coach.privacy.application.port.out.DataExportRequestRepository;
import com.training.coach.privacy.domain.model.DataExportRequest;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory DataExportRequestRepository for fast tests.
 */
public class InMemoryDataExportRequestRepository implements DataExportRequestRepository {
    private final ConcurrentHashMap<String, DataExportRequest> requests = new ConcurrentHashMap<>();

    @Override
    public DataExportRequest save(DataExportRequest request) {
        requests.put(request.id(), request);
        return request;
    }

    @Override
    public Optional<DataExportRequest> findById(String requestId) {
        return Optional.ofNullable(requests.get(requestId));
    }

    @Override
    public List<DataExportRequest> findByAthleteId(String athleteId) {
        return requests.values().stream()
                .filter(r -> r.athleteId().equals(athleteId))
                .collect(Collectors.toList());
    }

    @Override
    public List<DataExportRequest> findPending() {
        return requests.values().stream()
                .filter(DataExportRequest::isPending)
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
