package org.alexmond.sample.controller;

import org.alexmond.sample.config.ConfigSample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class GenerateJsonSchema {

    @Autowired
    private ApplicationContext context;

    private final Map<String, Object> mappingsMap = new HashMap<>();

    @GetMapping("/configSchema")
    public String Config(){
        this.mappingsMap.putAll(context.getBeansWithAnnotation(ConfigurationProperties.class));

        for (Map.Entry<String, Object> entry : this.mappingsMap.entrySet()) {
            Object config = entry.getValue();
            ConfigurationProperties annotation = AnnotationUtils.findAnnotation(config.getClass(), ConfigurationProperties.class);
            if (annotation != null) {
                System.out.println("Annotation value (AnnotationUtils): " + annotation.value());
            }
        }
        return "configSample";
    }
}
