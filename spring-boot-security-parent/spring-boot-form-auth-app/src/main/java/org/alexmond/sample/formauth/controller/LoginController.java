package org.alexmond.sample.formauth.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.alexmond.sample.formauth.service.ApplicationAccessService;
import org.alexmond.sample.formauth.service.UserConfigService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@Slf4j
public class LoginController {

    private final AuthenticationManager authenticationManager;
    private final ApplicationAccessService applicationAccessService;
    private final UserConfigService userConfigService;

    @GetMapping("/login")
    public String showLoginForm(Model model) {
        model.addAttribute("applications", 
            userConfigService.getAllUsers().stream()
                .flatMap(user -> user.getApplications().stream())
                .distinct()
                .collect(Collectors.toList())
        );
        return "login";
    }

    @GetMapping("/login-error")
    public String loginError(Model model) {
        model.addAttribute("error", "Invalid credentials or application access denied");
        model.addAttribute("applications", 
            userConfigService.getAllUsers().stream()
                .flatMap(user -> user.getApplications().stream())
                .distinct()
                .collect(Collectors.toList())
        );
        return "login";
    }

    @PostMapping("/login")
    public String processLogin(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String applicationName,
            Model model) {

        log.info("Login attempt for user: {} to access application: {}", username, applicationName);

        // Validate application access
        if (!applicationAccessService.validateApplicationAccess(username, applicationName)) {
            log.warn("Access denied for user: {} to application: {}", username, applicationName);
            return "redirect:/login-error";
        }

        // Authenticate user
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.info("User: {} successfully authenticated for application: {}", username, applicationName);
            return "redirect:/hello?app=" + applicationName;
        } catch (AuthenticationException e) {
            log.error("Authentication failed for user: {}", username);
            return "redirect:/login-error";
        }
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/login";
    }
}
