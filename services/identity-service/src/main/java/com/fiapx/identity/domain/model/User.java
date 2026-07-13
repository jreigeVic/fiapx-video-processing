package com.fiapx.identity.domain.model;

import java.time.Instant;
import java.util.UUID;

/** Domain entity placeholder. No business rules implemented. */
public class User {
    private UUID id;
    private String name;
    private String email;
    private String passwordHash;
    private Instant createdAt;

    // Getters and setters omitted for brevity in scaffold
}

