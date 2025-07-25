package org.alexmond.gen;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "app")
@Data
public class BootDocConfig {

    private String version;
    private String genericMeta;
    private String outputDir;
    private String indexFile;
    private String propFile;
    private List<PropertyDocument> documents = new ArrayList<>();
    private Map<String, List<String>> mapping = new HashMap<>();
    private Map<String, String> types = new HashMap<>();

    @PostConstruct
    public void init() {
        mapping.forEach((k, v) -> v.forEach(t -> types.put(t, k)));
    }

    @Data
    public static class PropertyDocument {
        private String id;
        private Integer order;
        private String header;
        private List<String> source;
        private String schemaFile;
    }
}
