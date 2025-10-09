package org.alexmond.sample.auth;

import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Collections;

@Component
public class CustomRestAuthenticationProvider implements AuthenticationProvider {

    private final RestClient restClient = RestClient.builder()
            .baseUrl("http://localhost:8081/api/auth")
            .build();

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();

        AuthRequest request = new AuthRequest(username, password);

        AuthResponse response = restClient.post()
                .uri("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(AuthResponse.class);

        if ("Authentication successful".equalsIgnoreCase(response.message())) {
            return new UsernamePasswordAuthenticationToken(username, password, Collections.emptyList());
        } else {
            throw new BadCredentialsException("External authentication failed");
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }

    // DTOs matching the remote service
    public record AuthRequest(String username, String password) {
    }

    public record AuthResponse(String message) {
    }

}
