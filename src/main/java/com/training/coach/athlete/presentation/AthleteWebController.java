package com.training.coach.athlete.presentation;

import com.training.coach.athlete.application.service.AthleteService;
import com.training.coach.athlete.domain.model.Athlete;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/athletes")
public class AthleteWebController {

    private final AthleteService athleteService;

    public AthleteWebController(AthleteService athleteService) {
        this.athleteService = athleteService;
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
            model.addAttribute("athlete", result.value().get());
            return "athletes/view";
        }
        return "redirect:/athletes";
    }
}
