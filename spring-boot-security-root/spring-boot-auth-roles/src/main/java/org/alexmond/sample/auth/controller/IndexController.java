package org.alexmond.sample.auth.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {
    @GetMapping({"/", "/index"})
    public String index(Model model) {
        model.addAttribute("title", "Index Page");
        model.addAttribute("userLinkText", "User Page");
        model.addAttribute("adminLinkText", "Admin Page");
        return "index";
    }
}
