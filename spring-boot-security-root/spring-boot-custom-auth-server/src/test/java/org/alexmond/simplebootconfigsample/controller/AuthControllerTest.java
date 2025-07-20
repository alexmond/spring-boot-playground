package org.alexmond.simplebootconfigsample.controller;

import org.alexmond.simplebootconfigsample.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.mockito.Mockito.when;

@WebMvcTest(AuthController.class)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    void shouldAuthenticateSuccessfullyWhenCredentialsAreValid() throws Exception {
        // Arrange
        String validUsername = "testUser";
        String validPassword = "securePass123";
        when(userService.authenticate(validUsername, validPassword)).thenReturn(true);

        String requestBody = """
                {
                    "username": "testUser",
                    "password": "securePass123"
                }
                """;

        // Act and Assert
        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Authentication successful"));
    }

    @Test
    void shouldReturnBadRequestWhenCredentialsAreInvalid() throws Exception {
        // Arrange
        String invalidUsername = "invalidUser";
        String invalidPassword = "wrongPass";
        when(userService.authenticate(invalidUsername, invalidPassword)).thenReturn(false);

        String requestBody = """
                {
                    "username": "invalidUser",
                    "password": "wrongPass"
                }
                """;

        // Act and Assert
        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Authentication failed"));
    }

    @Test
    void shouldReturnBadRequestWhenRequestBodyIsInvalid() throws Exception {
        // Arrange
        String invalidRequestBody = """
                {
                    "user": "testUser"
                }
                """;

        // Act and Assert
        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequestBody))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }
}