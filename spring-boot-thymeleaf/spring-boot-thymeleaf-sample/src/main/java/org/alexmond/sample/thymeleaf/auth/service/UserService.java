package org.alexmond.sample.thymeleaf.auth.service;

import org.alexmond.sample.thymeleaf.auth.model.Address;
import org.alexmond.sample.thymeleaf.auth.model.User;
import org.alexmond.sample.thymeleaf.auth.model.UserRole;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class UserService {
    
    private final List<User> users = new ArrayList<>();
    private final AtomicLong idGenerator = new AtomicLong(1);
    
    public UserService() {
        // Initialize with sample data
        users.add(new User(idGenerator.getAndIncrement(), "John", "Doe", "john.doe@example.com",
                LocalDate.of(1990, 5, 15), true, UserRole.ADMIN,
                Arrays.asList("Programming", "Reading", "Gaming"),
                new Address("123 Main St", "New York", "NY", "10001", "USA")));
        
        users.add(new User(idGenerator.getAndIncrement(), "Jane", "Smith", "jane.smith@example.com",
                LocalDate.of(1985, 8, 22), true, UserRole.USER,
                Arrays.asList("Photography", "Travel", "Cooking"),
                new Address("456 Oak Ave", "Los Angeles", "CA", "90001", "USA")));
        
        users.add(new User(idGenerator.getAndIncrement(), "Mike", "Johnson", "mike.johnson@example.com",
                LocalDate.of(1992, 12, 3), false, UserRole.MODERATOR,
                Arrays.asList("Sports", "Music", "Movies"),
                new Address("789 Pine Rd", "Chicago", "IL", "60601", "USA")));
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
}
