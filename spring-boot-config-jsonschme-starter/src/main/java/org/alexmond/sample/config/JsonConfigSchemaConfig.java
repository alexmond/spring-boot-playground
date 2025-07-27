package org.alexmond.sample.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "configschema")
@Data
public class JsonConfigSchemaConfig {

    private String schemaSpec = "http://json-schema.org/draft-07/schema#";
    private String schemaId = "your-schema-id";
    private String title = "Spring Boot Configuration Properties";
    private String description =  "Auto-generated schema from configuration metadata";

    private boolean useOpenapi = true;
    private boolean useValidation = true;

    private List<String> additionalProperties = List.of("logging");

}
