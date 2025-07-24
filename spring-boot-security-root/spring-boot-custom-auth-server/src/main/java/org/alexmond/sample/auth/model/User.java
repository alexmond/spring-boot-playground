package org.alexmond.sample.auth.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.alexmond.sample.auth.config.UserConfig;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Long id;
    private String username;
    private String password;
    private String firstName;
    private String lastName;
    private String email;
    private boolean active;
    private UserRole role;

    public User(Long id, UserConfig config) {
        this.id = id;
        this.username = config.getUsername();
        this.password = config.getPassword();
        this.firstName = config.getFirstName();
        this.lastName = config.getLastName();
        this.email = config.getEmail();
        this.active = config.isActive();
        this.role = config.getRole();
    }


    public String getFullName() {
        return firstName + " " + lastName;
    }
    
    
}
