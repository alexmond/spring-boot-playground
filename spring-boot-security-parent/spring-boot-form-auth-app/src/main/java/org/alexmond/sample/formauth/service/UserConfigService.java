package org.alexmond.sample.formauth.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.alexmond.sample.formauth.model.User;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@EnableConfigurationProperties(UserConfigService.UsersConfig.class)
@RequiredArgsConstructor
@Slf4j
public class UserConfigService {

    private final UsersConfig usersConfig;

    public List<User> getAllUsers() {
        return usersConfig.getUsers();
    }

    @ConfigurationProperties(prefix = "app")
    @Getter
    public static class UsersConfig {
        private List<User> users = new ArrayList<>();

        public void setUsers(List<User> users) {
            this.users = users;
            log.info("Loaded {} users from configuration", users.size());
        }
    }
}
