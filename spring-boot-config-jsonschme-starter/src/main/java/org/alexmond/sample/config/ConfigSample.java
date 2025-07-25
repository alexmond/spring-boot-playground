package org.alexmond.sample.config;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


@Schema(description = "Sample enumeration values")
enum EnumSample {EN1, EN2, EN3}

@Component
@ConfigurationProperties(prefix = "sample")
@Validated
@Data
@Schema(description = "Configuration sample class")
public class ConfigSample {
    // Java doc style comments required for config metadata processor
    /** String sample **/
    @Schema(description = "Sample string property", defaultValue = "stringSample")
    String stringSample = "stringSample";
    /**
     * Boolean sample
     **/
    @Schema(description = "Sample boolean property", defaultValue = "true")
    Boolean booleanSample = true;
    /**
     * Integer sample
     **/
    @Min(10)
    @Max(200)
    @Schema(description = "Sample integer property", minimum = "10", maximum = "200", defaultValue = "100")
    Integer integerSample = 100;

    @Schema(description = "Sample enum property", defaultValue = "EN1")
    EnumSample enumSample = EnumSample.EN1;

    @Schema(description = "Sample string collection")
    Collection<String> collectionSample = new ArrayList<>();
    @Schema(description = "Sample nested configuration collection")
    Collection<ConfigSampleNested> configSampleNesteds = new ArrayList<>();
    @Schema(description = "Sample string map")
    Map<String, String> mapSample = new HashMap<>();

    /**
     * Nested class sample
     **/
    @NestedConfigurationProperty // required for config metadata processor
    @Schema(description = "Nested configuration sample")
    ConfigSampleNested configSampleNested = new ConfigSampleNested();
}
