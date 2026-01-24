package com.training.coach.testconfig.inmemory;

import com.training.coach.athlete.application.port.out.TestResultRepository;
import com.training.coach.athlete.domain.model.FtpTestResult;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory TestResultRepository for fast tests.
 */
public class InMemoryTestResultRepository implements TestResultRepository {
    private final ConcurrentHashMap<String, List<FtpTestResult>> testResults = new ConcurrentHashMap<>();

    @Override
    public FtpTestResult save(FtpTestResult testResult) {
        testResults.computeIfAbsent(testResult.ftp().toString() + "_" + testResult.testDate().toString(),
                key -> new java.util.ArrayList<>()).add(testResult);
        return testResult;
    }

    @Override
    public List<FtpTestResult> findByAthleteId(String athleteId) {
        return testResults.values().stream()
                .flatMap(List::stream)
                .filter(test -> test.ftp().toString().contains(athleteId))
                .collect(Collectors.toList());
    }

    @Override
    public java.util.Optional<FtpTestResult> findById(String testResultId) {
        return testResults.values().stream()
                .flatMap(List::stream)
                .filter(test -> test.ftp().toString().equals(testResultId))
                .findFirst();
    }

    /**
     * Clear all test results for testing purposes.
     */
    public void clearAll() {
        testResults.clear();
    }
}