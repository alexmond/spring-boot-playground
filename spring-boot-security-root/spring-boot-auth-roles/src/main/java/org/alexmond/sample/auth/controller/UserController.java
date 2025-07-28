package org.alexmond.sample.auth.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UserController {
    @GetMapping("/user")
    public String user(Model model) {
        model.addAttribute("title", "User Page");
        return "user";
    }
}
