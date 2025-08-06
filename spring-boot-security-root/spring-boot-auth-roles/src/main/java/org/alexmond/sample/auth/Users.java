package org.alexmond.sample.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Component
@ConfigurationProperties("users")
public class Users {
    public List<AppUser> appUsers;

    public Collection<UserDetails> getUserDetails() {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        Collection<UserDetails> userDetails = new ArrayList<>();
        if (appUsers != null) {
            for (AppUser appUser : appUsers) {
                userDetails.add(User.withUsername(appUser.getUsername())
                    .password(appUser.getPassword())
                    .roles(appUser.getRoles().toArray(new String[0]))
                    .build());
            }
        }
        return userDetails;
    }
}