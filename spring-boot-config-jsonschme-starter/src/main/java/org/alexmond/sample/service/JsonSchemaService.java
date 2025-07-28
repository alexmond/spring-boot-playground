package org.alexmond.sample.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class JsonSchemaService {

    private final ConfigurationPropertyCollector propertyCollector;
    private final ConfigurationMetadataService metadataService;
    private final JsonSchemaBuilder schemaBuilder;
    private final ObjectMapper mapper;

    public JsonSchemaService(ConfigurationPropertyCollector propertyCollector,
                           ConfigurationMetadataService metadataService,
                           JsonSchemaBuilder schemaBuilder,
                           ObjectMapper mapper) {
        this.propertyCollector = propertyCollector;
        this.metadataService = metadataService;
        this.schemaBuilder = schemaBuilder;
        this.mapper = mapper;
    }

    public String generateFullSchema() throws Exception {
        List<String> included = propertyCollector.collectIncludedPropertyNames();
        ConfigurationMetadataRepository repository = metadataService.loadAllMetadata();
        metadataService.logConfigurationProperties(repository);
        
        Map<String, Object> schema = schemaBuilder.buildSchema(repository, included);
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(schema);
    }
}
