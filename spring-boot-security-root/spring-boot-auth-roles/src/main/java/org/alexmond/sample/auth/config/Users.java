package org.alexmond.sample.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;

@Component
@ConfigurationProperties("users")
@Data
public class Users {
    public Collection<AdminUser> adminUsers;

    public Collection<UserDetails> getUserDetails(){
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        Collection<UserDetails> userDetails = new ArrayList<>();
        for(AdminUser adminUser : adminUsers){
            userDetails.add(User.withUsername(adminUser.getUser()).password(passwordEncoder.encode(adminUser.getPassword())).roles(adminUser.getRole().toString()).build());
        }
        return userDetails;
    }

}
