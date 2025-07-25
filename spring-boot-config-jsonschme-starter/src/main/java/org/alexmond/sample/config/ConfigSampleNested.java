package org.alexmond.sample.config;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(description = "Configuration class containing nested configuration properties")
public class ConfigSampleNested {
    /* Nested Config sample */
    @Schema(description = "First nested configuration property", example = "nestedConfig1")
    String nestedConfig1 = "nestedConfig1";

}
