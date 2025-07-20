package org.alexmond.simplebootconfigsample.config;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.alexmond.simplebootconfigsample.model.UserRole;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
public class UserConfig {
    @NotEmpty
    private String username;
    @NotEmpty
    private String password;
    private String firstName = null;
    private String lastName = null;
    private String email = null;
    private boolean active = true;
    private UserRole role;
}
