package org.alexmond.sample.oauth2;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
class OAuth2ServerApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void contextLoads() {
    }

    @Test
    void shouldAccessHomeEndpoint() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void shouldAccessUserInfoWithAuthentication() throws Exception {
        mockMvc.perform(get("/userinfo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authorities").isArray());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void shouldReturnAdminAuthoritiesInUserInfo() throws Exception {
        mockMvc.perform(get("/userinfo"))
                .andExpect(status().isOk());
    }
}
