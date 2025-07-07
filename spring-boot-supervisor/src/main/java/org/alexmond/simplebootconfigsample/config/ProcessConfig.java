package org.alexmond.simplebootconfigsample.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.URL;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Configuration class for process execution settings.
 * Contains settings for command execution, environment variables,
 * working directory and logging configuration.
 */
@Data
@Validated
@ConfigurationProperties("process")
@Component
public class ProcessConfig {
    /**
     * The name of the process configuration.
     * Used as an identifier for the process configuration instance.
     */
    @NotNull
    String name;
    /**
     * The command to be executed. This field is required and cannot be empty.
     * Can be either a full path to an executable or a command available in the system PATH.
     */
    @NotBlank(message = "Command must not be empty")
    private String command;

    /**
     * The working directory where the process will be executed.
     * If not specified, the current working directory will be used.
     */
    private String workingDirectory;

    /**
     * URL for health checking the process.
     * Should be a valid URL that the process exposes for health monitoring.
     */
    @URL(message = "Health URL must be a valid URL")
    private String healthUrl;

    /**
     * Environment variables to be set for the process.
     * Keys represent environment variable names, values represent their corresponding values.
     */
    private Map<String, String> env = new HashMap<>();

    /**
     * Command-line arguments to be passed to the process.
     * Each element in the list represents a single argument.
     */
    private List<String> args = new ArrayList<>();

    /**
     * File path where the standard output (stdout) of the process will be logged.
     * If not specified, stdout will be inherited from the parent process.
     */
    private String stdoutLogfile;

    /**
     * File path where the standard error (stderr) of the process will be logged.
     * If not specified, stderr will be inherited from the parent process.
     */
    private String stderrLogfile;

    private Boolean redirectErrorStream = true;

}