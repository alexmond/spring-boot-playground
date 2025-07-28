package org.alexmond.sample.controller;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.extern.slf4j.Slf4j;
import org.alexmond.sample.service.JsonSchemaService;
import org.alexmond.sample.service.ConfigurationPropertyCollector;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;
import java.util.Set;

@Component
@Slf4j
public class JsonSchemaGenerator {

    private static final String JSON_SCHEMA_FILE = "sample-schema.json";
    private static final String YAML_SCHEMA_FILE = "sample-schema.yaml";

    private final JsonSchemaService jsonSchemaService;
    private final ConfigurationPropertyCollector propertyCollector;

    public JsonSchemaGenerator(JsonSchemaService jsonSchemaService,
                              ConfigurationPropertyCollector propertyCollector) {
        this.jsonSchemaService = jsonSchemaService;
        this.propertyCollector = propertyCollector;
    }

    public String generate() throws Exception {
        String jsonSchemaString = jsonSchemaService.generateFullSchema();
        
        ObjectMapper jsonMapper = new ObjectMapper();
        ObjectWriter jsonWriter = jsonMapper.writer(new DefaultPrettyPrinter());
        jsonWriter.writeValue(Paths.get(JSON_SCHEMA_FILE).toFile(), jsonMapper.readTree(jsonSchemaString));
        
        ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
        ObjectWriter yamlWriter = yamlMapper.writer(new DefaultPrettyPrinter());
        log.info("Writing yaml schema");
        yamlWriter.writeValue(Paths.get(YAML_SCHEMA_FILE).toFile(), jsonMapper.readTree(jsonSchemaString));
        
        return jsonSchemaString;
    }

    public Set<String> getAllPropertyKeys() {
        return propertyCollector.getAllPropertyKeys();
    }
}
