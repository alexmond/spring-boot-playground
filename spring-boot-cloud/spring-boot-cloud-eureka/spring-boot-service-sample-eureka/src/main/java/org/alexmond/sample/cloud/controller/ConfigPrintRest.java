package org.alexmond.sample.cloud.controller;

import org.alexmond.sample.cloud.config.ConfigSample;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ConfigPrintRest {

 private final ConfigSample configSample;

    public ConfigPrintRest(ConfigSample configSample) {
        this.configSample = configSample;
    }

    @GetMapping("/")
    public ConfigSample Config(){
        return configSample;
    }
}
