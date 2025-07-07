package org.alexmond.simplebootconfigsample.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.Collection;


/**
 * Configuration class for supervisor settings.
 * Maps properties with 'supervisor' prefix from configuration files
 * to this class fields.
 */
@Component
@ConfigurationProperties(prefix = "supervisor")
@Validated
@Data
public class SupervisorConfig {

    String nodeName = "supervisor";

    /**
     * Collection of process configurations to be supervised
     */
    Collection<ProcessConfig> process = new ArrayList<>();

}
