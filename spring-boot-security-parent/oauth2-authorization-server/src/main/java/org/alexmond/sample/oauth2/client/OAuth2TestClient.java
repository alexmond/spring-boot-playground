package org.alexmond.sample.oauth2.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.Map;

/**
 * OAuth2 Test Client for testing authorization server endpoints
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class OAuth2TestClient {

    private final RestTemplate restTemplate;

    public OAuth2TestClient() {
        this.restTemplate = new RestTemplateBuilder()
                .rootUri("http://localhost:9000")
                .build();
    }

    /**
     * Get token using Client Credentials grant type
     * @param clientId OAuth2 client ID
     * @param clientSecret OAuth2 client secret
     * @return Token response containing access_token
     */
    public Map<String, Object> getTokenUsingClientCredentials(String clientId, String clientSecret) {
        log.info("Requesting token with client credentials: {}", clientId);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");
        body.add("scope", "read write admin");

        ResponseEntity<Map> response = restTemplate.postForEntity("/oauth2/token", body, Map.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            log.info("Token obtained successfully");
            return response.getBody();
        } else {
            log.error("Failed to obtain token: {}", response.getStatusCode());
            return null;
        }
    }

}
