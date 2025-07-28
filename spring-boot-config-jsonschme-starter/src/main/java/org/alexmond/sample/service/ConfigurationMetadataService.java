package org.alexmond.sample.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataRepository;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataRepositoryJsonBuilder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

@Service
@Slf4j
public class ConfigurationMetadataService {

    public ConfigurationMetadataRepository loadAllMetadata() throws IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        List<InputStream> additionalStreams = collectAdditionalMetadataStreams(classLoader);
        List<InputStream> streams = collectMetadataStreams(classLoader);

        List<InputStream> allStreams = new ArrayList<>();
        allStreams.addAll(streams);
//        allStreams.addAll(additionalStreams);
        try {
            return ConfigurationMetadataRepositoryJsonBuilder.create(allStreams.toArray(new InputStream[0])).build();
        } finally {
            closeStreams(streams);
        }
    }

    private List<InputStream> collectMetadataStreams(ClassLoader classLoader) throws IOException {
        List<InputStream> streams = new ArrayList<>();
        
        Enumeration<URL> resources = classLoader.getResources("META-INF/spring-configuration-metadata.json");
        while (resources.hasMoreElements()) {
            URL url = resources.nextElement();
            log.debug("Found configuration metadata file: {}", url);
            streams.add(url.openStream());
        }
        return streams;
    }

    private List<InputStream> collectAdditionalMetadataStreams(ClassLoader classLoader) throws IOException {
        List<InputStream> streams = new ArrayList<>();

        Enumeration<URL> additionalResources = classLoader.getResources("META-INF/additional-spring-configuration-metadata.json");
        while (additionalResources.hasMoreElements()) {
            URL url = additionalResources.nextElement();
            log.debug("Found additional configuration metadata file: {}", url);
            streams.add(url.openStream());
        }
        return streams;
    }

    private void closeStreams(List<InputStream> streams) {
        for (InputStream stream : streams) {
            try {
                stream.close();
            } catch (IOException ignored) {
                log.warn("Failed to close metadata stream");
            }
        }
    }

    public void logConfigurationProperties(ConfigurationMetadataRepository repository) {
        repository.getAllProperties().forEach((name, property) -> 
            log.debug("{} = {}", name, property.getType()));
    }
}
