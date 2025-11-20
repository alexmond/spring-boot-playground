package org.alexmond.sample.formauth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ApplicationAccessService {

    private final UserService userService;

    public boolean validateApplicationAccess(String username, String applicationName) {
        if (applicationName == null || applicationName.trim().isEmpty()) {
            return false;
        }
        return userService.hasApplicationAccess(username, applicationName);
    }
}
