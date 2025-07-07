package org.alexmond.simplebootconfigsample.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;

/**
 * Represents a user entity in the system.
 * Implements Identifiable interface for consistent ID handling.
 */
@Data
public class User implements Identifiable<Integer> {
    private final Integer id;

    @NotBlank(message = "Name cannot be blank")
    private String name;

    /**
     * Creates a new User instance.
     *
     * @param id   unique identifier for the user
     * @param name user's name (must not be blank)
     * @throws IllegalArgumentException if name is blank
     */
    public User(Integer id, String name) {
        this.id = id;
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be blank");
        }
        this.name = name;
    }
}

/**
 * Interface for entities that have an identifier.
 *
 * @param <T> the type of the identifier
 */
interface Identifiable<T> {
    T getId();
}