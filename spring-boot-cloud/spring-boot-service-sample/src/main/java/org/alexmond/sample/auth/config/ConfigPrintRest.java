package org.alexmond.sample.auth.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ConfigPrintRest {
@Autowired ConfigSample configSample;

    @GetMapping("/config")
    public ConfigSample Config(){
        return configSample;
    }
}
