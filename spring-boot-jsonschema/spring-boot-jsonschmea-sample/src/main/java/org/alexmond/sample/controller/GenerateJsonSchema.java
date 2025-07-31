package org.alexmond.sample.controller;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.extern.slf4j.Slf4j;
import org.alexmond.config.json.schema.metaextension.BootConfigMetaLoader;
import org.alexmond.config.json.schema.service.JsonSchemaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Paths;
import java.util.*;

@RestController
@Slf4j
public class GenerateJsonSchema {

    @Autowired
    private ApplicationContext context;


    @Autowired
    private JsonSchemaService jsonSchemaService;

    @Autowired
    private BootConfigMetaLoader bootConfigMetaLoader;

    private final Map<String, Object> mappingsMap = new HashMap<>();

    @GetMapping("/configSchema")
    public String Config() throws Exception {

        String jsonConfigSchema;

        jsonConfigSchema = jsonSchemaService.generateFullSchema();

        // Save as JSON
        ObjectMapper jsonMapper = new ObjectMapper();
        ObjectWriter jsonWriter = jsonMapper.writer(new DefaultPrettyPrinter());
        log.info("Writing json schema");
        jsonWriter.writeValue(Paths.get("sample-schema.json").toFile(), jsonMapper.readTree(jsonConfigSchema));

        // Save as YAML 
        ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
        ObjectWriter yamlWriter = yamlMapper.writer(new DefaultPrettyPrinter());
        log.info("Writing yaml schema");
        yamlWriter.writeValue(Paths.get("sample-schema.yaml").toFile(), jsonMapper.readTree(jsonConfigSchema));
        

        return "configSample";
    }


}
