package org.alexmond.gen;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataProperty;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataRepositoryJsonBuilder;
import org.springframework.core.io.FileUrlResource;
import org.springframework.core.io.ResourceLoader;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.*;
import java.net.URL;
import java.nio.file.Paths;
import java.util.*;

@SpringBootApplication
public class BootDocGenerate implements CommandLineRunner {

    private static final Logger LOG = LoggerFactory.getLogger(BootDocGenerate.class);
    private static final String OBJECT = "object";
    private static final String PROPERTIES = "properties";

    @Autowired
    ResourceLoader resourceLoader;
    @Autowired
    private BootDocConfig bootDocConfig;
    @Autowired
    private TemplateEngine templateEngine;

    public static void main(String[] args) {
        SpringApplication.run(BootDocGenerate.class);
    }

    @Override
    public void run(String... args) {
        Set<URL> paths = getResourceFolderFiles();

        File f = new File(bootDocConfig.getOutputDir());
        f.mkdir();

        TemplateProperties templateProperties = new TemplateProperties();
        templateProperties.setVersion(bootDocConfig.getVersion());

        bootDocConfig.getDocuments().sort(Comparator.comparing(BootDocConfig.PropertyDocument::getOrder));
        for (BootDocConfig.PropertyDocument doc : bootDocConfig.getDocuments()) {
            if (null != paths) {
                LOG.info("Generating documentation for " + doc.getHeader());
                templateProperties.getApps().add(generateDocument(doc, getInputStreams(paths, doc.getSource())));
                generateScema(doc, getInputStreams(paths, doc.getSource()));
            } else {
                LOG.error("No metadata found for " + doc.getHeader());
            }
        }

        Context context = new Context();
        context.setVariable("prop", templateProperties);

        try (Writer writer = new FileWriter(bootDocConfig.getOutputDir() + "/" + bootDocConfig.getIndexFile())) {
            writer.write(templateEngine.process(bootDocConfig.getIndexFile(), context));
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }

        try (Writer writer = new FileWriter(bootDocConfig.getOutputDir() + "/" + bootDocConfig.getPropFile())) {
            writer.write(templateEngine.process(bootDocConfig.getPropFile(), context));
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private TemplateAppProperties generateDocument(BootDocConfig.PropertyDocument doc, InputStream... resources) {

        TemplateAppProperties templateAppProperties = new TemplateAppProperties();
        templateAppProperties.setHeading(doc.getHeader());
        templateAppProperties.setTitle(doc.getHeader());
        templateAppProperties.setId(doc.getId());
        templateAppProperties.setOrder(doc.getOrder().toString());

        try {
            Map<String, ConfigurationMetadataProperty> configProps = ConfigurationMetadataRepositoryJsonBuilder
                    .create(resources)
                    .build().getAllProperties();
            configProps.forEach((name, prop) -> templateAppProperties.getProperties()
                    .add(new TemplateAppProperties.AppProperty(name, prop.getDefaultValue(), prop.getDescription())));
        } catch (IOException e) {
            LOG.error("Error reading file", e);
        }

        return templateAppProperties;
    }

    private InputStream[] getInputStreams(Set<URL> urls, List<String> source) {
        List<InputStream> resources = new ArrayList<>();
        urls.forEach(url ->
                source.forEach(regex -> {
                    if (url.getPath().contains(regex)) {
                        try {
                            LOG.info("Matched regex: " + regex + ", path: " + url);
                            resources.add(new FileUrlResource(url).getInputStream());
                        } catch (IOException e) {
                            LOG.error("Error ", e);
                        }
                    }
                })
        );
        return resources.toArray(new InputStream[0]);
    }

    private void generateScema(BootDocConfig.PropertyDocument doc, InputStream... resources) {
        Map<String, Object> schema = new HashMap<>();

        // create object mapper instance
        ObjectMapper mapper = new ObjectMapper();
        ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());

        schema.put("$schema", "http://json-schema.org/draft-07/schema#");
        schema.put("id", doc.getId()); //  "$id": "mighty/db"
        schema.put("title", doc.getHeader());
        schema.put("description", doc.getHeader());
        schema.put("type", OBJECT);
        schema.put(PROPERTIES, new HashMap<>());

        try {
            Map<String, ConfigurationMetadataProperty> configProps = ConfigurationMetadataRepositoryJsonBuilder
                    .create(resources)
                    .build().getAllProperties();
            configProps.forEach((name, prop) -> addNode(name.split("\\."), 0, (Map) schema.get(PROPERTIES), prop));

            writer.writeValue(Paths.get(bootDocConfig.getOutputDir() + "/" + doc.getId() + ".json").toFile(), schema); ///// ====== TODO
        } catch (Exception e) {
            LOG.error("Error reading file", e);
        }
    }

    private String BootTypeConverter(String type) {
        return bootDocConfig.getTypes().containsKey(type) ? bootDocConfig.getTypes().get(type) : OBJECT;
    }

    public void addNode(String[] nodes, int i, Map node, ConfigurationMetadataProperty prop) {
        if (i == nodes.length) return;
        String current = nodes[i];
        if (i == nodes.length - 1) {
            node.put(nodes[i], getCurrentNode(prop));
        } else {
            if (!node.containsKey(current)) {
                HashMap currentNode = new HashMap();
                currentNode.put(PROPERTIES, new HashMap());
                currentNode.put("type", OBJECT);
                node.put(current, currentNode);
            }
            addNode(nodes, ++i, (Map) ((Map) node.get(current)).get(PROPERTIES), prop);
        }
    }

    private HashMap getCurrentNode(ConfigurationMetadataProperty prop) {
        HashMap<String, Object> currentNode = new HashMap<>();
        if (null != prop.getDescription())
            currentNode.put("description", prop.getDescription());

        if (null != prop.getDefaultValue())
            currentNode.put("default", prop.getDefaultValue());

        if (null != prop.getType())
            currentNode.put("type", BootTypeConverter(prop.getType()));
        else
            currentNode.put("type", OBJECT);
        return currentNode;
    }

    private Set<URL> getResourceFolderFiles() {
        Set<URL> paths = new HashSet<>();
        try {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            Enumeration<URL> e = loader.getResources("META-INF/spring-configuration-metadata.json");
            while (e.hasMoreElements()) {
                paths.add(e.nextElement());
            }
        } catch (Exception e) {
            LOG.error("Error reading resources",e);
        }

        paths.forEach((URL path) -> {
            LOG.info("Found metadata: " + path.getPath());
        });
        return paths;
    }

}
