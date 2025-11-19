package org.alexmond.sample.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.alexmond.sample.auth.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    @Autowired
    private final UserService userService;

    @Operation(summary = "Authenticate user", description = "Authenticates a user with username and password")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully authenticated"),
            @ApiResponse(responseCode = "400", description = "Authentication failed")
    })
    @PostMapping("/login")
    public ResponseEntity<?> authenticate(
            @Parameter(description = "Authentication credentials") @RequestBody AuthRequest request) {
        log.info("Authentication attempt for user: {}", request.getUsername());
        boolean isAuthenticated = userService.authenticate(request.getUsername(), request.getPassword());

        if (isAuthenticated) {
            log.info("Authentication successful for user: {}", request.getUsername());
            return ResponseEntity.ok(new AuthResponse("Authentication successful"));
        }
        log.info("Authentication failed for user: {}", request.getUsername());
        return ResponseEntity.badRequest().body(new AuthResponse("Authentication failed"));
    }

    private record AuthRequest(String username, String password) {
        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }
    }

    private record AuthResponse(String message) {
    }

}
