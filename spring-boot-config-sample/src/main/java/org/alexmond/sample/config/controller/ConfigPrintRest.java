package org.alexmond.sample.config.controller;

import org.alexmond.sample.config.config.ConfigSample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ConfigPrintRest {
    @Autowired
    ConfigSample configSample;

    @GetMapping("/")
    public ConfigSample Config() {

        return configSample;
    }
}
