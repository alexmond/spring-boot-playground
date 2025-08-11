package org.alexmond.sample.auth.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    private UserDetails getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            return (UserDetails) authentication.getPrincipal();
        }
        return null;
    }

    private String getRoleName(UserDetails user) {
        return user.getAuthorities().stream().map(a -> a.getAuthority()).findFirst().get();
    }

    @GetMapping("/user")
    public String user(Model model) {
        UserDetails user = getCurrentUser();
        if (user != null) {
            model.addAttribute("username", user.getUsername());
            model.addAttribute("role", getRoleName(user));
        }
        model.addAttribute("title", "User Page");
        model.addAttribute("indexLinkText", "Index Page");
        return "user";
    }

    @GetMapping({"/", "/index"})
    public String index(Model model) {
        model.addAttribute("title", "Index Page");
        model.addAttribute("userLinkText", "User Page");
        model.addAttribute("adminLinkText", "Admin Page");
        return "index";
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/admin")
    public String admin(Model model) {
        UserDetails user = getCurrentUser();
        if (user != null) {
            model.addAttribute("username", user.getUsername());
            model.addAttribute("role", getRoleName(user));
        }
        model.addAttribute("title", "Admin Page");
        model.addAttribute("indexLinkText", "Index Page");
        return "admin";
    }
}