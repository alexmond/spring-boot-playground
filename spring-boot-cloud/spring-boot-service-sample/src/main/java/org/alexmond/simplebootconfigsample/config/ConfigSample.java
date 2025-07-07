package org.alexmond.simplebootconfigsample.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;


@Component
@ConfigurationProperties(prefix = "sample")
@Validated
@Data
public class ConfigSample {
    // Java doc style comments required for config metadata processor
    /* String sample */
    String config1 = "config1";
    String config2 = "config2";
    String config3 = "config3";
}
