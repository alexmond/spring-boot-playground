package org.alexmond.sample.cloud.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.*;


enum EnumSample {EN1, EN2, EN3}

@Component
@ConfigurationProperties(prefix = "sample")
@Validated
@Data
public class ConfigSample {
    // Java doc style comments required for config metadata processor
    /** String sample **/
    String stringSample = "stringSample";
    /** Boolean sample **/
    Boolean booleanSample = true;
    /** Integer sample **/
    @Min(10)
    @Max(200)
    Integer integerSample = 100;

    EnumSample enumSample = EnumSample.EN1;

    List<String> collectionSample = new ArrayList<>();
    List<ConfigSampleNested> configSampleNesteds = new ArrayList<>();
    Map<String, String> mapSample = new HashMap<>();

    /** Nested class sample **/
    @NestedConfigurationProperty // required for config metadata processor
    ConfigSampleNested configSampleNested = new ConfigSampleNested();
}
