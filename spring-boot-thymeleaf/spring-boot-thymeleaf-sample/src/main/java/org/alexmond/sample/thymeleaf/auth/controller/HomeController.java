package org.alexmond.sample.thymeleaf.auth.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("title", "Thymeleaf Demo Application");
        model.addAttribute("message", "Welcome to the Thymeleaf Features Demonstration!");
        return "index";
    }
}
