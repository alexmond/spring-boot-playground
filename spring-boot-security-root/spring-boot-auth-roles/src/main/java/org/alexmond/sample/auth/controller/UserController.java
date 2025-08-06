package org.alexmond.sample.auth.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@PreAuthorize( "hasRole('ROLE_USER')")
@Controller
public class UserController {
    @GetMapping("/user")
    public String user(Model model, @AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails principal) {
        model.addAttribute("username", principal.getUsername());
        model.addAttribute("title", "User Page");
        model.addAttribute("indexLinkText", "Index Page");
        return "user";
    }
}