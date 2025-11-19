package org.alexmond.sample.oauth2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * OAuth2 Authorization Server Application
 * Provides OAuth2 and OpenID Connect (OIDC) authentication
 */
@SpringBootApplication
public class OAuth2ServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(OAuth2ServerApplication.class, args);
    }
}