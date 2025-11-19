package org.alexmond.sample.oauth2.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@Slf4j
@Tag(name = "User Info", description = "User information endpoints")
public class UserInfoController {

    @GetMapping("/userinfo")
    @Operation(summary = "Get user information", description = "Returns authenticated user information")
    @ApiResponse(responseCode = "200", description = "User information retrieved successfully")
    public Map<String, Object> userInfo(Authentication authentication) {
        log.info("User info requested for: {}", authentication.getName());

        Map<String, Object> userInfo = new HashMap<>();

        if (authentication.getPrincipal() instanceof Jwt jwt) {
            userInfo.put("sub", jwt.getSubject());
            userInfo.put("preferred_username", jwt.getClaimAsString("preferred_username"));
            userInfo.put("email", jwt.getClaimAsString("email"));
            userInfo.put("name", jwt.getClaimAsString("name"));
        } else {
            userInfo.put("username", authentication.getName());
        }

        List<String> authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        userInfo.put("authorities", authorities);

        return userInfo;
    }


    @GetMapping("/")
    @Operation(summary = "Home endpoint", description = "OAuth2 server home page")
    @ApiResponse(responseCode = "200", description = "Server information")
    public Map<String, String> home() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Welcome to OAuth2 Authorization Server!");
        response.put("info", "Available endpoints: /oauth2/authorize, /oauth2/token, /userinfo, /.well-known/openid-configuration");
        response.put("documentation", "http://localhost:9000/swagger-ui.html");
        response.put("h2-console", "http://localhost:9000/h2-console");
        return response;
    }

}

