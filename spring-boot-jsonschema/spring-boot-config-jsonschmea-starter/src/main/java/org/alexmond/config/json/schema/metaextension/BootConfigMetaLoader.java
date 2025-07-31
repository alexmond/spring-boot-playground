package org.alexmond.config.json.schema.metaextension;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.alexmond.config.json.schema.metamodel.BootConfigMeta;
import org.alexmond.config.json.schema.metamodel.Hint;
import org.alexmond.config.json.schema.metamodel.Property;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

@NoArgsConstructor
@Slf4j
public class BootConfigMetaLoader {
    public BootConfigMeta loadFromFile(String path) throws Exception {
        log.info("Loading configuration from file: {}", path);
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(new File(path), BootConfigMeta.class);
    }

    public  BootConfigMeta loadFromStream(InputStream stream) throws Exception {
        log.info("Loading configuration from input stream");
        ObjectMapper mapper = new ObjectMapper();
        BootConfigMeta config;
        config = mapper.readValue(stream, BootConfigMeta.class);
        return config;
    }

    public HashMap<String, Property> loadFromInputStreams(List<InputStream> streams, List<InputStream> additionalStreams) throws Exception {
        log.info("Loading and merging configuration from {} input streams", streams.size());
        HashMap<String, Property> propertyMap;
        HashMap<String, Property> adPropertyMap = new HashMap<>();

        propertyMap = loadMetadata(streams);
        adPropertyMap = loadMetadata(additionalStreams);

        return propertyMap;
    }

    private HashMap<String,Property> loadMetadata(List<InputStream> streams) throws Exception {
        HashMap<String, Property> propertyMap = new HashMap<>();
        BootConfigMeta mergedConfig = new BootConfigMeta();

        for (InputStream stream : streams) {
            if (stream != null) {
                BootConfigMeta config = new BootConfigMetaLoader().loadFromStream(stream);
                // Merge the configurations
                if (config.getGroups() != null) {
                    mergedConfig.getGroups().addAll(config.getGroups());
                }
                if (config.getProperties() != null) {
                    mergedConfig.getProperties().addAll(config.getProperties());
                }
            }
        }

        for (Property property : mergedConfig.getProperties()) {
            if (!propertyMap.containsKey(property.getName())) {
                propertyMap.put(property.getName(), property);
            }else{
                log.error("Duplicate property name: " + property.getName());
            }
        }

        for (Hint hint : mergedConfig.getHints()) {
            if (propertyMap.containsKey(hint.getName())) {
                Property property = propertyMap.get(hint.getName());
                property.setHint(hint);
            }else{
                log.error("Missing property name for a hint: " + hint.getName());
            }
        }
        return propertyMap;
    }
}
