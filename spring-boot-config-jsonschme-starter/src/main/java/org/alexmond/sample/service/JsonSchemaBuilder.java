
package org.alexmond.sample.service;

import lombok.extern.slf4j.Slf4j;
import org.alexmond.sample.config.JsonConfigSchemaConfig;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataProperty;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataRepository;
import org.springframework.boot.configurationmetadata.Deprecation;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class JsonSchemaBuilder {

    private final JsonConfigSchemaConfig config;
    private final TypeMappingService typeMappingService;

    public JsonSchemaBuilder(JsonConfigSchemaConfig config, TypeMappingService typeMappingService) {
        this.config = config;
        this.typeMappingService = typeMappingService;
    }

    public Map<String, Object> buildSchema(ConfigurationMetadataRepository repository, List<String> included) {
        log.info("Starting JSON schema generation");
        Map<String, Object> schema = new LinkedHashMap<>();

        schema.put("$schema", config.getSchemaSpec());
        schema.put("$id", config.getSchemaId());
        schema.put("title", config.getTitle());
        schema.put("description", config.getDescription());
        schema.put("type", "object");

        Map<String, Object> properties = new LinkedHashMap<>();
        for (ConfigurationMetadataProperty prop : repository.getAllProperties().values()) {
            if (!matchesIncluded(prop.getId(), included)) continue;
            addProperty(properties, prop.getId().split("\\."), 0, prop);
        }
        schema.put("properties", properties);

        return schema;
    }

    private void addProperty(Map<String, Object> node, String[] path, int idx, ConfigurationMetadataProperty prop) {
        log.debug("Processing property at path: {}, index: {}", String.join(".", path), idx);
        if (node == null) {
            log.error("Null node encountered while adding property at path: {}, index: {}", String.join(".", path), idx);
            throw new IllegalArgumentException("Node must not be null in addProperty: idx=" + idx + ", path=" + String.join(".", path));
        }
        String key = path[idx];
        if (idx == path.length - 1) {
            Map<String, Object> propDef = new LinkedHashMap<>();
            // skip deprecated property with error level
            if(prop.isDeprecated() && prop.getDeprecation().getLevel() == Deprecation.Level.ERROR ){
                log.debug("Skipping property is deprecated and removed: {}", prop.getName());
                return;
            }
            if(prop.getType() == null) {
                log.error("property {} prop.type is null", prop.getName());
                return;
            }
            propDef.put("type", typeMappingService.mapType(prop.getType()));
            if (prop.getDescription() != null) {
                propDef.put("description", prop.getDescription());
            }
            if (prop.getShortDescription() != null) {
                propDef.put("title", prop.getShortDescription());
            }
            if (prop.getDefaultValue() != null) {
                propDef.put("default", prop.getDefaultValue());
            }
            processHints(propDef, prop);
            processEnumValues(propDef, prop);
            processDeprecation(propDef, prop);

            if (typeMappingService.mapType(prop.getType()).equals("array")) {
                String itemType = typeMappingService.extractListItemType(prop.getType());
                Map<String, Object> items = new HashMap<>();
                items.put("type", typeMappingService.mapType(itemType));
                if (typeMappingService.mapType(itemType).equals("object")) {
                    Map<String, Object> complexProperties = typeMappingService.processComplexType(itemType);
                    if (complexProperties != null) {
                        items.put("properties", complexProperties);
                    }
                }
                propDef.put("items", items);
            } else if (typeMappingService.mapType(prop.getType()).equals("object")) {
                if (prop.getType().startsWith("java.util.Map")) {
                    String valueType = typeMappingService.extractMapValueType(prop.getType());
                    if (typeMappingService.mapType(valueType).equals("object")) {
                        Map<String, Object> valueTypeProperties = typeMappingService.processComplexType(valueType);
                        if (valueTypeProperties != null) {
                            propDef.put("additionalProperties", Map.of(
                                    "type", "object",
                                    "properties", valueTypeProperties
                            ));
                        }
                    } else {
                        propDef.put("additionalProperties", Map.of("type", typeMappingService.mapType(valueType)));
                    }
                } else {
                    Map<String, Object> complexProperties = typeMappingService.processComplexType(prop.getType());
                    if (complexProperties != null) {
                        propDef.put("properties", complexProperties);
                    }
                }
            }

            node.put(key, propDef);
        } else {
            Map<String, Object> obj = (Map<String, Object>) node.computeIfAbsent(key, k -> {
                Map<String, Object> nm = new HashMap<>();
                nm.put("type", "object");
                nm.put("properties", new HashMap<String, Object>());
                return nm;
            });
            Object propsObj = obj.get("properties");
            if (!(propsObj instanceof Map)) {
                propsObj = new HashMap<String, Object>();
                obj.put("properties", propsObj);
            }
            Map<String, Object> child = (Map<String, Object>) propsObj;
            addProperty(child, path, idx + 1, prop);
        }
    }

    private boolean matchesIncluded(String propertyPath, List<String> included) {
        for (String include : included) {
            if (propertyPath.startsWith(include)) return true;
        }
        return false;
    }

    private void processHints(Map<String, Object> propDef, ConfigurationMetadataProperty prop) {
        log.debug("Processing hints for property: {}", prop.getName());
        if (prop.getHints() != null) {
            if (prop.getHints().getValueHints() != null && !prop.getHints().getValueHints().isEmpty()) {
                var hints = prop.getHints().getValueHints().stream()
                        .map(hint -> hint.getValue())
                        .filter(value -> value != null)
                        .toList();
                if (hints.size() > 1) {
                    log.debug("Property '{}' has multiple hints: {}", prop.getName(), hints);
                }
                propDef.put("examples", hints);
            }
        }
    }

    private void processEnumValues(Map<String, Object> propDef, ConfigurationMetadataProperty prop) {
        log.debug("Processing enum values for property: {}", prop.getName());
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
        log.debug("Processing deprecation for property: {}", prop.getName());
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
