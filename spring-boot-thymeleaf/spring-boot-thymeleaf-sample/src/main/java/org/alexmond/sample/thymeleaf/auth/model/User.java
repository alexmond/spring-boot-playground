package org.alexmond.sample.thymeleaf.auth.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private LocalDate birthDate;
    private boolean active;
    private UserRole role;
    private List<String> interests;
    private Address address;

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
