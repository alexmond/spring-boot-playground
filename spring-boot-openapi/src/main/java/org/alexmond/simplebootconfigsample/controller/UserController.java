package org.alexmond.simplebootconfigsample.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import org.alexmond.simplebootconfigsample.model.User;
import org.alexmond.simplebootconfigsample.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Tag(name = "User Services", description = "User API")
@RestController
@RequestMapping("/api/v1")
public class UserController {
    private final AtomicInteger counter = new AtomicInteger();
    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/users")
    @Operation(summary = "Returns all users", tags = {"User"})
    @ApiResponse(responseCode = "200", description = "Returns all users",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = User.class)))
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a new user")
    @ApiResponse(responseCode = "201", description = "User successfully created",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = User.class)))
    public User register(@RequestParam(defaultValue = "Stranger") String name) {
        User newUser = new User(counter.incrementAndGet(), name);
        return userRepository.addUser(newUser);
    }

    @PutMapping("/users/{id}")
    @Operation(summary = "Update a user's name")
    @ApiResponse(responseCode = "200", description = "User successfully updated",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = User.class)))
    public User updateUser(@PathVariable int id, @RequestParam String newName) {
        return userRepository.updateUser(id, newName)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
    }

    @DeleteMapping("/users/{id}")
    @Operation(summary = "Delete a user")
    @ApiResponse(responseCode = "200", description = "User successfully deleted",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = boolean.class)))
    public boolean deleteUser(@PathVariable int id) {
        return userRepository.deleteUser(id);
    }
}