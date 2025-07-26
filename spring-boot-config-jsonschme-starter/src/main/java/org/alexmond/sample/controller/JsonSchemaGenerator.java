package org.alexmond.sample.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataProperty;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataRepository;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Component
@Slf4j
public class JsonSchemaGenerator {

    private final ObjectMapper mapper;

    private String toSnakeCase(String input) {
        if (input == null) return input;
        String regex = "([a-z])([A-Z])";
        String replacement = "$1_$2";
        return input.replaceAll(regex, replacement).toLowerCase();
    }

    private Map<String, Object> processComplexType(String type) {
        try {
            Class<?> clazz = Class.forName(type);
            Map<String, Object> properties = new HashMap<>();
            for (java.lang.reflect.Field field : clazz.getDeclaredFields()) {
                Map<String, Object> fieldDef = new HashMap<>();
                fieldDef.put("type", mapType(field.getType().getName()));
                properties.put(toSnakeCase(field.getName()), fieldDef);
            }
            return properties;
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    public JsonSchemaGenerator(ObjectMapper mapper) {
        log.info("Initializing JsonSchemaGenerator");
        this.mapper = mapper;
    }

    public String toJsonSchema(ConfigurationMetadataRepository repository, List<String> included,
                               String schemaId, String title, String description) throws Exception {
        log.info("Starting JSON schema generation with id: {}, title: {}", schemaId, title);
        Map<String, Object> schema = new HashMap<>();
        schema.put("$schema", "http://json-schema.org/draft-07/schema#");
        schema.put("$id", schemaId);
        schema.put("title", title);
        schema.put("description", description);
        schema.put("type", "object");

        Map<String, Object> propertiesNode = new HashMap<>();

        for (Map.Entry<String, ConfigurationMetadataProperty> entry : repository.getAllProperties().entrySet()) {
            String propertyPath = entry.getKey();
            if (included == null || included.isEmpty() || matchesIncluded(propertyPath, included)) {
                addProperty(propertiesNode, propertyPath.split("\\."), 0, entry.getValue());
            }
        }

        schema.put("properties", propertiesNode);

        try {
            String result = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(schema);
            log.info("Successfully generated JSON schema");
            return result;
        } catch (Exception e) {
            log.error("Failed to generate JSON schema", e);
            throw e;
        }
    }

    private void addProperty(Map<String, Object> node, String[] path, int idx, ConfigurationMetadataProperty prop) {
        log.info("Processing property at path: {}, index: {}", String.join(".", path), idx);
        if (node == null) {
            // Defensive: always expect a valid map
            log.error("Null node encountered while adding property at path: {}, index: {}", String.join(".", path), idx);
            throw new IllegalArgumentException("Node must not be null in addProperty: idx=" + idx + ", path=" + String.join(".", path));
        }
        String key = path[idx];
        if (idx == path.length - 1) {
            Map<String, Object> propDef = new HashMap<>();
            propDef.put("type", mapType(prop.getType()));
            if (prop.getDescription() != null) propDef.put("description", prop.getDescription());
            if (prop.getDefaultValue() != null) propDef.put("default", prop.getDefaultValue());

            processHints(propDef, prop);
            processEnumValues(propDef, prop);
            processDeprecation(propDef, prop);

            if (mapType(prop.getType()).equals("object")) {
                Map<String, Object> complexProperties = processComplexType(prop.getType());
                if (complexProperties != null) {
                    propDef.put("properties", complexProperties);
                }
            }

            node.put(key, propDef);
        } else {
            // Ensure node structure
            Map<String, Object> obj = (Map<String, Object>) node.computeIfAbsent(key, k -> {
                Map<String, Object> nm = new HashMap<>();
                nm.put("type", "object");
                nm.put("properties", new HashMap<String, Object>());
                return nm;
            });
            // Ensure "properties" map exists and is a Map
            Object propsObj = obj.get("properties");
            if (!(propsObj instanceof Map)) {
                propsObj = new HashMap<String, Object>();
                obj.put("properties", propsObj);
            }
            Map<String, Object> child = (Map<String, Object>) propsObj;
            addProperty(child, path, idx + 1, prop);
        }
    }

    private String mapType(String springType) {
        log.info("Mapping Spring type: {}", springType);
        if (springType == null) return "string";
        if (springType.equals("java.lang.Integer") || springType.equals("int")) return "integer";
        if (springType.equals("java.lang.Boolean") || springType.equals("boolean")) return "boolean";
        if (springType.equals("java.lang.Double") || springType.equals("double") ||
                springType.equals("java.lang.Float") || springType.equals("float")) return "number";
        if (springType.startsWith("java.util.List") || springType.startsWith("java.util.Set")) return "array";
        try {
            Class<?> type = Class.forName(springType);
            if (type.isEnum()) return "string";
            if (!type.isPrimitive() && !type.getName().startsWith("java.lang.")) return "object";
        } catch (ClassNotFoundException e) {
            if (springType.contains("Enum")) return "string";
        }
        return "string";
    }

    private boolean matchesIncluded(String propertyPath, List<String> included) {
        log.info("Checking if property path {} matches included paths", propertyPath);
        return included.stream().anyMatch(prefix ->
                propertyPath.equals(prefix) || propertyPath.startsWith(prefix + "."));
    }

    private void processHints(Map<String, Object> propDef, ConfigurationMetadataProperty prop) {
        log.info("Processing hints for property: {}", prop.getName());
        if (prop.getHints() != null) {
            if (prop.getHints().getValueHints() != null && !prop.getHints().getValueHints().isEmpty()) {
                var hints = prop.getHints().getValueHints().stream()
                        .map(hint -> hint.getValue())
                        .filter(value -> value != null)
                        .toList();
                if (hints.size() > 1) {
                    log.info("Property '{}' has multiple hints: {}", prop.getName(), hints);
                }
                propDef.put("examples", hints);
            }
        }
    }

    private void processEnumValues(Map<String, Object> propDef, ConfigurationMetadataProperty prop) {
        log.info("Processing enum values for property: {}", prop.getName());
        if (prop.getType() != null) {
            try {
                Class<?> type = Class.forName(prop.getType());
                if (type.isEnum()) {
                    Object[] enumConstants = type.getEnumConstants();
                    if (enumConstants != null) {
                        List<String> enumValues = Arrays.stream(enumConstants)
                                .flatMap(enumConstant -> Arrays.stream(new String[]{
                                        enumConstant.toString(),
                                        enumConstant.toString().toLowerCase()
                                }))
                                .toList();
                        propDef.put("enum", enumValues);
                        return;
                    }
                }
            } catch (ClassNotFoundException e) {
                // Fall back to hints-based enum detection
                if (prop.getType().contains("Enum")) {
                    if (prop.getHints() != null && prop.getHints().getValueHints() != null) {
                        List<String> enumValues = prop.getHints().getValueHints().stream()
                                .flatMap(hint -> Arrays.stream(new String[]{
                                        hint.getValue().toString(),
                                        hint.getValue().toString().toLowerCase()
                                }))
                                .toList();
                        if (!enumValues.isEmpty()) {
                            propDef.put("enum", enumValues);
                        }
                    }
                }
            }
        }
    }

    private void processDeprecation(Map<String, Object> propDef, ConfigurationMetadataProperty prop) {
        log.info("Processing deprecation for property: {}", prop.getName());
        if (prop.isDeprecated()) {
            propDef.put("deprecated", true);
            if (prop.getDeprecation() != null) {
                if (prop.getDeprecation().getReason() != null) {
                    propDef.put("deprecationReason", prop.getDeprecation().getReason());
                }
                if (prop.getDeprecation().getReplacement() != null) {
                    propDef.put("deprecationReplacement", prop.getDeprecation().getReplacement());
                }
            }
        }
    }
}