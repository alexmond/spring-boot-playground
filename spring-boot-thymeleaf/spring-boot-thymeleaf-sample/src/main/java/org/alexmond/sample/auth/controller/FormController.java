package org.alexmond.sample.auth.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/forms")
public class FormController {
    
    @GetMapping("/demo")
    public String showFormDemo(Model model) {
        model.addAttribute("title", "Form Elements Demo");
        model.addAttribute("countries", Arrays.asList("USA", "Canada", "UK", "Germany", "France"));
        model.addAttribute("hobbies", Arrays.asList("Reading", "Gaming", "Sports", "Music", "Travel"));
        return "forms/demo";
    }
    
    @PostMapping("/demo")
    public String processForm(@RequestParam String name,
                            @RequestParam String email,
                            @RequestParam String country,
                            @RequestParam(required = false) List<String> hobbies,
                            @RequestParam String message,
                            Model model) {
        model.addAttribute("title", "Form Submission Result");
        model.addAttribute("name", name);
        model.addAttribute("email", email);
        model.addAttribute("country", country);
        model.addAttribute("hobbies", hobbies);
        model.addAttribute("message", message);
        return "forms/result";
    }
}
