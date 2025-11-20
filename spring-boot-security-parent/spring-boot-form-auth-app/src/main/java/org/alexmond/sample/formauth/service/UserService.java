package org.alexmond.sample.formauth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.alexmond.sample.formauth.model.CustomUserDetails;
import org.alexmond.sample.formauth.model.User;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserConfigService userConfigService;
    private final PasswordEncoder passwordEncoder;

    public CustomUserDetails loadUserByUsername(String username) {
        List<User> users = userConfigService.getAllUsers();
        
        return users.stream()
                .filter(user -> user.getUsername().equals(username))
                .map(user -> new CustomUserDetails(
                    user.getUsername(),
                    user.getPassword(),
                    user.getApplications()
                ))
                .findFirst()
                .orElse(null);
    }

    public boolean authenticateUser(String username, String password) {
        CustomUserDetails userDetails = loadUserByUsername(username);
        if (userDetails == null) {
            log.warn("User not found: {}", username);
            return false;
        }

        boolean isAuthenticated = passwordEncoder.matches(password, userDetails.getPassword());
        if (isAuthenticated) {
            log.info("User authenticated successfully: {}", username);
        } else {
            log.warn("Invalid password for user: {}", username);
        }
        return isAuthenticated;
    }

    public boolean hasApplicationAccess(String username, String applicationName) {
        CustomUserDetails userDetails = loadUserByUsername(username);
        if (userDetails == null) {
            return false;
        }
        boolean hasAccess = userDetails.getApplications().contains(applicationName);
        log.info("User: {} has access to application: {} = {}", username, applicationName, hasAccess);
        return hasAccess;
    }
}
