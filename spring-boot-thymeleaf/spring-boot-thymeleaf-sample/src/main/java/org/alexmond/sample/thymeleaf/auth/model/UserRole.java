package org.alexmond.sample.thymeleaf.auth.model;

public enum UserRole {
    ADMIN("Administrator"),
    USER("Regular User"),
    MODERATOR("Moderator");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
