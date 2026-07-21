package com.fiapx.identity.domain.model;

import java.time.Instant;
import java.util.UUID;

public final class User {

    private final UUID id;
    private final String name;
    private final Email email;
    private final PasswordHash passwordHash;
    private final Instant createdAt;

    private User(UUID id, String name, Email email, PasswordHash passwordHash, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.createdAt = createdAt;
    }

    public static User register(String name, Email email, PasswordHash passwordHash) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name must not be blank");
        }
        return new User(UUID.randomUUID(), name.trim(), email, passwordHash, Instant.now());
    }

    public static User reconstruct(
            UUID id, String name, Email email, PasswordHash passwordHash, Instant createdAt) {
        return new User(id, name, email, passwordHash, createdAt);
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Email getEmail() {
        return email;
    }

    public PasswordHash getPasswordHash() {
        return passwordHash;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
