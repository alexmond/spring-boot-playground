package org.alexmond.sample.auth.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@PreAuthorize( "hasRole('ROLE_ADMIN')")
@Controller
public class AdminController {
    @GetMapping("/admin")
    public String admin(Model model) {
        model.addAttribute("title", "Admin Page");
        model.addAttribute("indexLinkText", "Index Page");
        return "admin";
    }
}
