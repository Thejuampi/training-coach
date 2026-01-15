package com.training.coach.sync.presentation;

import com.training.coach.sync.application.service.SyncService;
import java.time.LocalDate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sync")
public class SyncController {

    private final SyncService syncService;

    public SyncController(SyncService syncService) {
        this.syncService = syncService;
    }

    @PostMapping("/athletes/{athleteId}")
    public ResponseEntity<Void> syncAthleteData(
            @PathVariable String athleteId, @RequestParam(defaultValue = "30") int daysBack) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(daysBack);
        syncService.syncAthleteData(athleteId, startDate, endDate);
        return ResponseEntity.ok().build();
    }
}
