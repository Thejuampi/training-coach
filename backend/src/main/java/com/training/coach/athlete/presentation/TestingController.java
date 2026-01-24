package com.training.coach.athlete.presentation;

import com.training.coach.athlete.application.service.TestingService;
import com.training.coach.athlete.domain.model.FtpTestResult;
import java.time.LocalDate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for testing operations.
 */
@RestController
@RequestMapping("/api/testing")
public class TestingController {

    private final TestingService testingService;

    public TestingController(TestingService testingService) {
        this.testingService = testingService;
    }

    /**
     * Schedule an FTP test for an athlete.
     */
    @PostMapping("/{athleteId}/ftp-test/schedule")
    public ResponseEntity<Void> scheduleFtpTest(
            @PathVariable String athleteId,
            @RequestParam LocalDate testDate
    ) {
        testingService.scheduleFtpTest(athleteId, testDate);
        return ResponseEntity.accepted().build();
    }

    /**
     * Get test instructions for an FTP test.
     */
    @GetMapping("/ftp-test/instructions")
    public ResponseEntity<String> getFtpTestInstructions() {
        String instructions = testingService.getTestInstructions(TestingService.TestType.FTP_TEST);
        return ResponseEntity.ok(instructions);
    }

    /**
     * Record FTP test result for an athlete.
     */
    @PostMapping("/{athleteId}/ftp-test")
    public ResponseEntity<FtpTestResult> recordFtpTestResult(
            @PathVariable String athleteId,
            @RequestParam LocalDate testDate,
            @RequestParam double ftpValue
    ) {
        FtpTestResult result = testingService.recordFtpTestResult(athleteId, testDate, ftpValue);
        return ResponseEntity.ok(result);
    }

    /**
     * Get historical test results for an athlete.
     */
    @GetMapping("/{athleteId}/ftp-test/results")
    public ResponseEntity<java.util.List<FtpTestResult>> getHistoricalResults(@PathVariable String athleteId) {
        java.util.List<FtpTestResult> results = testingService.getHistoricalTestResults(athleteId);
        return ResponseEntity.ok(results);
    }

    /**
     * Get the latest FTP test result for an athlete.
     */
    @GetMapping("/{athleteId}/ftp-test/latest")
    public ResponseEntity<FtpTestResult> getLatestFtpTestResult(@PathVariable String athleteId) {
        return testingService.getLatestFtpTestResult(athleteId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Check if zone recalculation is needed after FTP update.
     */
    @GetMapping("/{athleteId}/needs-zone-recalculation")
    public ResponseEntity<Boolean> checkZoneRecalculation(
            @PathVariable String athleteId,
            @RequestParam LocalDate ftpUpdateDate
    ) {
        boolean needsRecalculation = testingService.needsZoneRecalculation(athleteId, ftpUpdateDate);
        return ResponseEntity.ok(needsRecalculation);
    }
}