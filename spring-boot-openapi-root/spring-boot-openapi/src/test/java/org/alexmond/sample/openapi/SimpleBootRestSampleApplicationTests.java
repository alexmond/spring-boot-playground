package org.alexmond.sample.openapi;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SimpleBootRestSampleApplicationTests {

    @Autowired
    private MockMvc mockMvc;


    @Test
    void contextLoads() {
    }

    @Test
    void generateOpenApiDocs() throws Exception {
        mockMvc.perform(get("/v3/api-docs.yaml")) // Or your custom api-docs path
                .andExpect(status().isOk())
                .andDo(result -> {
                    // You can save the result as a JSON/YAML file here
                    String apiDocs = result.getResponse().getContentAsString();
                    Files.writeString(Path.of("api-docs.yaml"), apiDocs);
                });
    }
}

