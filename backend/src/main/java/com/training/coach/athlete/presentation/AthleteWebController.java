package com.training.coach.athlete.presentation;

import com.training.coach.athlete.application.service.AthleteService;
import com.training.coach.athlete.application.service.EventService;
import com.training.coach.athlete.domain.model.Athlete;
import com.training.coach.athlete.domain.model.UserPreferences;
import com.training.coach.athlete.domain.model.Workout;
import com.training.coach.trainingplan.application.service.PlanService;
import java.time.LocalDate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/athletes")
public class AthleteWebController {

    private final AthleteService athleteService;
    private final PlanService planService;
    private final EventService eventService;

    public AthleteWebController(AthleteService athleteService, PlanService planService, EventService eventService) {
        this.athleteService = athleteService;
        this.planService = planService;
        this.eventService = eventService;
    }

    @GetMapping
    public String listAthletes(Model model) {
        model.addAttribute("athletes", athleteService.getAllAthletes());
        return "athletes/list";
    }

    @GetMapping("/new")
    public String newAthleteForm(Model model) {
        model.addAttribute("athlete", new Athlete("", "", null, null, null)); // dummy for form
        return "athletes/form";
    }

    @PostMapping
    public String createAthlete(Athlete athlete) {
        athleteService.createAthlete(athlete.name(), athlete.profile(), athlete.preferences());
        return "redirect:/athletes";
    }

    @GetMapping("/{id}")
    public String viewAthlete(@PathVariable String id, Model model) {
        var result = athleteService.getAthlete(id);
        if (result.isSuccess()) {
            var athlete = result.value().get();
            model.addAttribute("athlete", athlete);

            // Add today's workout if available
            var today = LocalDate.now();
            var workout = planService.getWorkoutForDate(id, today);
            model.addAttribute("todayWorkout", workout);

            return "athletes/view";
        }
        return "redirect:/athletes";
    }

    /**
     * View today's workout page.
     */
    @GetMapping("/{id}/workout/today")
    public String viewTodayWorkout(@PathVariable String id, Model model) {
        var result = athleteService.getAthlete(id);
        if (result.isSuccess()) {
            var athlete = result.value().get();
            model.addAttribute("athlete", athlete);

            var today = LocalDate.now();
            var workout = planService.getWorkoutForDate(id, today);
            model.addAttribute("workout", workout);

            if (workout == null) {
                model.addAttribute("noWorkout", true);
                return "athletes/no-workout";
            }

            return "athletes/today-workout";
        }
        return "redirect:/athletes";
    }

    /**
     * Update preferences form.
     */
    @GetMapping("/{id}/preferences")
    public String preferencesForm(@PathVariable String id, Model model) {
        var result = athleteService.getAthlete(id);
        if (result.isSuccess()) {
            var athlete = result.value().get();
            model.addAttribute("athlete", athlete);
            model.addAttribute("preferences", athlete.preferences());
            return "athletes/preferences";
        }
        return "redirect:/athletes";
    }

    /**
     * Submit preferences update.
     */
    @PostMapping("/{id}/preferences")
    public String updatePreferences(@PathVariable String id, @RequestParam UserPreferences preferences) {
        var result = athleteService.updateAthlete(id,
                new Athlete(id, null, null, null, preferences));

        if (result.isSuccess()) {
            // In a real implementation, this would also trigger a notification
            // if the new preferences conflict with the current plan
            return "redirect:/athletes/" + id + "?preferencesUpdated=true";
        }
        return "redirect:/athletes/" + id + "?preferencesUpdateError=true";
    }

    /**
     * View goal events.
     */
    @GetMapping("/{id}/events")
    public String viewEvents(@PathVariable String id, Model model) {
        // In a real implementation, this would fetch from eventService
        model.addAttribute("athleteId", id);
        return "athletes/events";
    }

    /**
     * Add goal event form.
     */
    @GetMapping("/{id}/events/new")
    public String newEventForm(@PathVariable String id, Model model) {
        model.addAttribute("athleteId", id);
        return "athletes/new-event";
    }

    /**
     * Submit new goal event.
     */
    @PostMapping("/{id}/events")
    public String createEvent(@PathVariable String id, @RequestParam String name,
                             @RequestParam String date, @RequestParam String priority) {
        // In a real implementation, this would call eventService
        return "redirect:/athletes/" + id + "/events?eventCreated=true";
    }
}
