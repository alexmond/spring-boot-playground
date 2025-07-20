package org.alexmond.simplebootconfigsample.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Data
@Component
@ConfigurationProperties("auth")
public class AuthConfig {
    private List<UserConfig> userConfigs = new ArrayList<>();
}
