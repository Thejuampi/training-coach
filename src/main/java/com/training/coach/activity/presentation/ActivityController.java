package com.training.coach.activity.presentation;

import com.training.coach.activity.application.service.ActivityReadService;
import com.training.coach.activity.domain.model.ActivityLight;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/activities")
public class ActivityController {

    private final ActivityReadService activityReadService;

    public ActivityController(ActivityReadService activityReadService) {
        this.activityReadService = activityReadService;
    }

    @GetMapping("/athletes/{athleteId}")
    public ResponseEntity<List<ActivityLight>> getActivities(
            @PathVariable String athleteId, @RequestParam LocalDate startDate, @RequestParam LocalDate endDate) {
        try {
            return ResponseEntity.ok(activityReadService.getActivities(athleteId, startDate, endDate));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/athletes/{athleteId}/date/{date}")
    public ResponseEntity<ActivityLight> getActivityByDate(
            @PathVariable String athleteId, @PathVariable LocalDate date) {
        return activityReadService
                .getActivityByDate(athleteId, date)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
