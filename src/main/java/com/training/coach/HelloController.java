package com.training.coach;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HelloController {

    @GetMapping("/")
    public String home() {
        return "redirect:/athletes";
    }

    @GetMapping("/hello")
    public String hello() {
        return "hello";
    }
}
