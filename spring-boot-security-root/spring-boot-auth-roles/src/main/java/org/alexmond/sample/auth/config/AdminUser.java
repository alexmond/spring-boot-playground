package org.alexmond.sample.auth.config;

import lombok.Data;

@Data
public class AdminUser {
    public String user;
    public String password;
    public Role role = Role.USER; // adding as default so no need to define for each user, we probably should add validation like NotNull
}
