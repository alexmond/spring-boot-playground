package org.alexmond.sample.controller;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.extern.slf4j.Slf4j;
import org.alexmond.sample.config.BootDocConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataRepository;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataRepositoryJsonBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Paths;
import java.util.*;

import static org.springframework.boot.Banner.Mode.LOG;

@RestController
@Slf4j
public class GenerateJsonSchema {

    @Autowired
    private ApplicationContext context;

    @Autowired
    ResourceLoader resourceLoader;

    @Autowired
    private BootDocConfig bootDocConfig;

    @Autowired
    private ConfigurableEnvironment env;

    @Autowired
    private JsonSchemaGenerator jsonSchemaGenerator;

    private final Map<String, Object> mappingsMap = new HashMap<>();

    @GetMapping("/configSchema")
    public String Config() throws Exception {
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

        included.add("logging");

        ConfigurationMetadataRepository repository = loadAllMetadata();
        repository.getAllProperties().forEach((name, property) -> {
            System.out.println(name + " = " + property.getType());
        });


        ConfigurationMetadataRepository repo = repository; // load repository (as in previous answer)
        String jsonSchemaString = jsonSchemaGenerator.toJsonSchema(
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
        

        return "configSample";
    }

//    private void processSpringMetadata(){
//        Set<URL> paths = getResourceFolderFiles();
//
//        bootDocConfig.getDocuments().sort(Comparator.comparing(BootDocConfig.PropertyDocument::getOrder));
//        for (BootDocConfig.PropertyDocument doc : bootDocConfig.getDocuments()) {
//            if (null != paths) {
//                log.info("Generating documentation for " + doc.getHeader());
//                generateScema(doc, getInputStreams(paths, doc.getSource()));
//            } else {
//                log.error("No metadata found for " + doc.getHeader());
//            }
//        }
//
//    }
//
//    private void generateScema(BootDocConfig.PropertyDocument doc, InputStream... resources) {
//        Map<String, Object> schema = new HashMap<>();
//
//        // create object mapper instance
//        ObjectMapper mapper = new ObjectMapper();
//        ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
//
//        schema.put("$schema", "http://json-schema.org/draft-07/schema#");
//        schema.put("id", doc.getId()); //  "$id": "mighty/db"
//        schema.put("title", doc.getHeader());
//        schema.put("description", doc.getHeader());
//        schema.put("type", OBJECT);
//        schema.put(PROPERTIES, new HashMap<>());
//
//        try {
//            Map<String, ConfigurationMetadataProperty> configProps = ConfigurationMetadataRepositoryJsonBuilder
//                    .create(resources)
//                    .build().getAllProperties();
//            configProps.forEach((name, prop) -> addNode(name.split("\\."), 0, (Map) schema.get(PROPERTIES), prop));
//
//            writer.writeValue(Paths.get(bootDocConfig.getOutputDir() + "/" + doc.getId() + ".json").toFile(), schema); ///// ====== TODO
//        } catch (Exception e) {
//            LOG.error("Error reading file", e);
//        }
//    }
//
//    private String BootTypeConverter(String type) {
//        return bootDocConfig.getTypes().containsKey(type) ? bootDocConfig.getTypes().get(type) : OBJECT;
//    }
//
//    public void addNode(String[] nodes, int i, Map node, ConfigurationMetadataProperty prop) {
//        if (i == nodes.length) return;
//        String current = nodes[i];
//        if (i == nodes.length - 1) {
//            node.put(nodes[i], getCurrentNode(prop));
//        } else {
//            if (!node.containsKey(current)) {
//                HashMap currentNode = new HashMap();
//                currentNode.put(PROPERTIES, new HashMap());
//                currentNode.put("type", OBJECT);
//                node.put(current, currentNode);
//            }
//            addNode(nodes, ++i, (Map) ((Map) node.get(current)).get(PROPERTIES), prop);
//        }
//    }
//
//    private HashMap getCurrentNode(ConfigurationMetadataProperty prop) {
//        HashMap<String, Object> currentNode = new HashMap<>();
//        if (null != prop.getDescription())
//            currentNode.put("description", prop.getDescription());
//
//        if (null != prop.getDefaultValue())
//            currentNode.put("default", prop.getDefaultValue());
//
//        if (null != prop.getType())
//            currentNode.put("type", BootTypeConverter(prop.getType()));
//        else
//            currentNode.put("type", OBJECT);
//        return currentNode;
//    }
//
//    private Set<URL> getResourceFolderFiles() {
//        Set<URL> paths = new HashSet<>();
//        try {
//            ClassLoader loader = Thread.currentThread().getContextClassLoader();
//            Enumeration<URL> e = loader.getResources("META-INF/spring-configuration-metadata.json");
//            while (e.hasMoreElements()) {
//                paths.add(e.nextElement());
//            }
//        } catch (Exception e) {
//            log.error("Error reading resources",e);
//        }
//
//        paths.forEach((URL path) -> {
//            log.error("Found metadata: " + path.getPath());
//        });
//        return paths;
//    }

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

}
