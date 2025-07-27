package org.alexmond.sample.controller;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.extern.slf4j.Slf4j;
import org.alexmond.sample.config.JsonConfigSchemaConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataProperty;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataRepository;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataRepositoryJsonBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Paths;
import java.util.*;


@Component
@Slf4j
public class JsonSchemaGenerator {

    private final ObjectMapper mapper;

    @Autowired
    private ApplicationContext context;

    @Autowired
    ResourceLoader resourceLoader;

    @Autowired
    private JsonConfigSchemaConfig jsonConfigSchemaConfig;

    @Autowired
    private ConfigurableEnvironment env;

    private final Map<String, Object> mappingsMap = new HashMap<>();

    public String generate() throws Exception {

        this.mappingsMap.putAll(context.getBeansWithAnnotation(ConfigurationProperties.class));

        List<String> included = new ArrayList<>();

        for (Map.Entry<String, Object> entry : this.mappingsMap.entrySet()) {
            Object config = entry.getValue();
            ConfigurationProperties annotation = AnnotationUtils.findAnnotation(config.getClass(), ConfigurationProperties.class);
            if (annotation != null) {
                System.out.println("Annotation value (AnnotationUtils): " + annotation.value());
                included.add(annotation.value());
            }
        }

        Set<String> keys = getAllPropertyKeys();
        for (String key : keys) {
            if (key != null) {
                System.out.println("Found property keys: " + key);
                included.add(key);
            }
        }

        included.addAll(jsonConfigSchemaConfig.getAdditionalProperties());

        ConfigurationMetadataRepository repository = loadAllMetadata();
        repository.getAllProperties().forEach((name, property) -> {
            System.out.println(name + " = " + property.getType());
        });


        ConfigurationMetadataRepository repo = repository; // load repository (as in previous answer)
        String jsonSchemaString = toJsonSchema(
                repo,
                included,
                "your-schema-id",
                "Spring Boot Configuration Properties",
                "Auto-generated schema from configuration metadata"
        );
        System.out.println("\n\n =========================== \n\n");


//        System.out.println(jsonSchemaString);

        // Save as JSON
        ObjectMapper jsonMapper = new ObjectMapper();
        ObjectWriter jsonWriter = jsonMapper.writer(new DefaultPrettyPrinter());
        jsonWriter.writeValue(Paths.get("sample-schema.json").toFile(), jsonMapper.readTree(jsonSchemaString));

        // Save as YAML
        ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
        ObjectWriter yamlWriter = yamlMapper.writer(new DefaultPrettyPrinter());
        log.info("Writing yaml schema");
        yamlWriter.writeValue(Paths.get("sample-schema.yaml").toFile(), jsonMapper.readTree(jsonSchemaString));


        return jsonSchemaString;
    }

    private String toSnakeCase(String input) {
        if (input == null) return input;
        String regex = "([a-z])([A-Z])";
        String replacement = "$1_$2";
        return input.replaceAll(regex, replacement).toLowerCase();
    }

    public Set<String> getAllPropertyKeys() {
        Set<String> keys = new HashSet<>();
        for (PropertySource<?> propertySource : env.getPropertySources()) {
            if (propertySource instanceof EnumerablePropertySource<?>) {
                String[] propertyNames = ((EnumerablePropertySource<?>) propertySource).getPropertyNames();
                for (String name : propertyNames) {
                    keys.add(name);
                }
            }
        }
        return keys;
    }


    public static ConfigurationMetadataRepository loadAllMetadata() throws IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Enumeration<URL> resources = classLoader.getResources("META-INF/spring-configuration-metadata.json");
        Enumeration<URL> resources2 = classLoader.getResources("META-INF/additional-spring-configuration-metadata.json");
        List<InputStream> streams = new ArrayList<>();

        while (resources.hasMoreElements()) {
            URL url = resources.nextElement();
            streams.add(url.openStream());
        }
        while (resources2.hasMoreElements()) {
            URL url = resources2.nextElement();
            streams.add(url.openStream());
        }

        try {
            return ConfigurationMetadataRepositoryJsonBuilder.create(streams.toArray(new InputStream[0])).build();
        } finally {
            for (InputStream stream : streams) {
                try {
                    stream.close();
                } catch (IOException ignored) {}
            }
        }
    }

    // Modified: Add visited parameter with Set to prevent recursion cycles
    private Map<String, Object> processComplexType(String type, Set<String> visited) {
        try {
            if (visited.contains(type)) {
                log.warn("Detected cyclic reference for type: {}. Skipping nested properties.", type);
                return new HashMap<>();
            }
            visited.add(type);

            Class<?> clazz = Class.forName(type);
            Map<String, Object> properties = new HashMap<>();
            for (java.lang.reflect.Field field : clazz.getDeclaredFields()) {
                Map<String, Object> fieldDef = new HashMap<>();
                String fieldType = field.getType().getName();
                fieldDef.put("type", mapType(fieldType));

                if (mapType(fieldType).equals("array")) {
                    String itemType = extractListItemType(field.getGenericType().getTypeName());
                    Map<String, Object> items = new HashMap<>();
                    items.put("type", mapType(itemType));
                    if (mapType(itemType).equals("object")) {
                        Map<String, Object> itemProperties = processComplexType(itemType, visited);
                        if (itemProperties != null) {
                            items.put("properties", itemProperties);
                        }
                    }
                    fieldDef.put("items", items);
                } else if (mapType(fieldType).equals("object")) {
                    if (fieldType.startsWith("java.util.Map")) {
                        String valueType = extractMapValueType(field.getGenericType().getTypeName());
                        if (mapType(valueType).equals("object")) {
                            Map<String, Object> valueTypeProperties = processComplexType(valueType, visited);
                            if (valueTypeProperties != null) {
                                fieldDef.put("additionalProperties", Map.of(
                                        "type", "object",
                                        "properties", valueTypeProperties
                                ));
                            }
                        } else {
                            fieldDef.put("additionalProperties", Map.of("type", mapType(valueType)));
                        }
                    } else {
                        Map<String, Object> nestedProperties = processComplexType(fieldType, visited);
                        if (nestedProperties != null) {
                            fieldDef.put("properties", nestedProperties);
                        }
                    }
                }

                properties.put(toSnakeCase(field.getName()), fieldDef);
            }
            visited.remove(type);
            return properties;
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    // For backwards compatibility, keeping old method signature
    private Map<String, Object> processComplexType(String type) {
        return processComplexType(type, new HashSet<>());
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
        schema.put("additionalProperties", false);

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

            if (mapType(prop.getType()).equals("array")) {
                String itemType = extractListItemType(prop.getType());
                Map<String, Object> items = new HashMap<>();
                items.put("type", mapType(itemType));
                if (mapType(itemType).equals("object")) {
                    Map<String, Object> complexProperties = processComplexType(itemType);
                    if (complexProperties != null) {
                        items.put("properties", complexProperties);
                    }
                }
                propDef.put("items", items);
            } else if (mapType(prop.getType()).equals("object")) {
                if (prop.getType().startsWith("java.util.Map")) {
                    String valueType = extractMapValueType(prop.getType());
                    if (mapType(valueType).equals("object")) {
                        Map<String, Object> valueTypeProperties = processComplexType(valueType);
                        if (valueTypeProperties != null) {
                            propDef.put("additionalProperties", Map.of(
                                    "type", "object",
                                    "properties", valueTypeProperties
                            ));
                        }
                    } else {
                        propDef.put("additionalProperties", Map.of("type", mapType(valueType)));
                    }
                } else {
                    Map<String, Object> complexProperties = processComplexType(prop.getType());
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

    private String mapType(String springType) {
        log.info("Mapping Spring type: {}", springType);
        if (springType == null) return "string";
        if (springType.equals("java.lang.Integer") || springType.equals("int")) return "integer";
        if (springType.equals("java.lang.Boolean") || springType.equals("boolean")) return "boolean";
        if (springType.equals("java.lang.Double") || springType.equals("double") ||
                springType.equals("java.lang.Float") || springType.equals("float")) return "number";
        if (springType.startsWith("java.util.List") || springType.startsWith("java.util.Set")) return "array";
        if (springType.startsWith("java.util.Map")) return "object";
        try {
            Class<?> type = Class.forName(springType);
            if (type.isEnum()) return "string";
            if (!type.isPrimitive() && !type.getName().startsWith("java.lang.")) return "object";
        } catch (ClassNotFoundException e) {
            if (springType.contains("Enum")) return "string";
        }
        return "string";
    }

    private String extractListItemType(String type) {
        if (type.contains("<") && type.contains(">")) {
            String genericType = type.substring(type.indexOf("<") + 1, type.indexOf(">"));
            return genericType.trim();
        }
        return "string";
    }

    private String extractMapValueType(String type) {
        if (type.contains("<") && type.contains(">")) {
            String genericTypes = type.substring(type.indexOf("<") + 1, type.indexOf(">"));
            String[] types = genericTypes.split(",");
            return types.length > 1 ? types[1].trim() : "string";
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