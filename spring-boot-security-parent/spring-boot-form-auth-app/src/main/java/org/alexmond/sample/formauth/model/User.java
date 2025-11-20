package org.alexmond.sample.formauth.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private  PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private String username;
    private String password;
    private List<String> applications;

    public void setPassword(String password) {
        this.password = passwordEncoder.encode(password) ;
    }

    public boolean hasAccessToApplication(String applicationName) {
        return applications != null && applications.contains(applicationName);
    }
}
