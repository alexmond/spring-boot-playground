package org.alexmond.sample.auth.service;

import org.alexmond.sample.auth.config.AuthConfig;
import org.alexmond.sample.auth.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class UserService {

    private final List<User> users = new ArrayList<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    private AuthConfig authConfig;

    @Autowired
    public UserService(AuthConfig authConfig) {
        // Initialize with sample data
        this.authConfig = authConfig;
        authConfig.getUserConfigs().forEach(config -> users.add(new User(idGenerator.getAndIncrement(), config)));
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(users);
    }

    public User getUserById(Long id) {
        return users.stream()
                .filter(user -> user.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public void saveUser(User user) {
        if (user.getId() == null) {
            user.setId(idGenerator.getAndIncrement());
            users.add(user);
        } else {
            int index = users.indexOf(getUserById(user.getId()));
            if (index != -1) {
                users.set(index, user);
            }
        }
    }

    public void deleteUser(Long id) {
        users.removeIf(user -> user.getId().equals(id));
    }

    public boolean authenticate(String username, String password) {
        return users.stream()
                .anyMatch(user -> user.getUsername().equals(username)
                        && user.getPassword().equals(password));
    }
}
