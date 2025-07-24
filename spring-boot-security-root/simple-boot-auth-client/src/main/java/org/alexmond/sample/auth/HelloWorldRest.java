package org.alexmond.sample.auth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

@RestController
@Slf4j
public class HelloWorldRest {
    @GetMapping("/")
    public String HelloWorld(){
        return "Hello World";
    }

    @GetMapping("/restlogin")
    public  String authrest() {
        RestClient restClient = RestClient.builder()
                .baseUrl("http://localhost:8081/api/auth")
                .build();

        // Create request
        AuthRequest request = new AuthRequest("test1", "test1");

        // Send POST request
        AuthResponse response = restClient.post()
                .uri("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(AuthResponse.class);

        // Print the response
        return response.message();
    }

    // DTOs (copy the definition to match your API)
    public record AuthRequest(String username, String password) {}
    public record AuthResponse(String message) {}

}
