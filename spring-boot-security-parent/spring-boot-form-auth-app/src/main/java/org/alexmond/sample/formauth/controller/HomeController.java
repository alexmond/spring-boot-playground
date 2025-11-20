package org.alexmond.sample.formauth.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Controller
@Slf4j
public class HomeController {

    @GetMapping("/hello")
    public String hello(@RequestParam(required = false) String app, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Prepare session information
        Map<String, Object> sessionInfo = new HashMap<>();
        sessionInfo.put("username", authentication.getName());
        sessionInfo.put("principal", authentication.getPrincipal());
        sessionInfo.put("authorities", authentication.getAuthorities());
        sessionInfo.put("authenticated", authentication.isAuthenticated());
        sessionInfo.put("credentialsNonExpired", true);
        sessionInfo.put("accountNonLocked", true);
        sessionInfo.put("accountNonExpired", true);
        sessionInfo.put("loginTime", LocalDateTime.now());
        sessionInfo.put("applicationName", app != null ? app : "Unknown");

        model.addAttribute("authentication", authentication);
        model.addAttribute("sessionInfo", sessionInfo);
        model.addAttribute("applicationName", app);

        log.info("User: {} accessed hello page for application: {}", authentication.getName(), app);
        return "hello";
    }
}
