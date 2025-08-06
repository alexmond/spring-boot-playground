package org.alexmond.sample.auth.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@PreAuthorize( "hasRole('ROLE_ADMIN')")
@Controller
public class AdminController {
    @GetMapping("/admin")
    public String admin(Model model, @AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails principal) {
        String role = principal.getAuthorities().stream()
                .map(auth -> auth.getAuthority())
                .filter(auth -> auth.startsWith("ROLE_"))
                .findFirst()
                .map(auth -> auth.replace("ROLE_", ""))
                .orElse("USER");  // default if not found

        model.addAttribute("username", principal.getUsername());
        model.addAttribute("role", role);
        model.addAttribute("title", "Admin Page");
        model.addAttribute("indexLinkText", "Index Page");
        return "admin";
    }
}
