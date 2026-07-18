package com.fiapx.identity.domain.model;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

public final class RefreshToken {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int RAW_VALUE_BYTE_LENGTH = 32;

    private final UUID id;
    private final UUID userId;
    private final String tokenHash;
    private final Instant expiresAt;
    private final Instant createdAt;
    private boolean revoked;

    private RefreshToken(
            UUID id,
            UUID userId,
            String tokenHash,
            Instant expiresAt,
            boolean revoked,
            Instant createdAt) {
        this.id = id;
        this.userId = userId;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
        this.revoked = revoked;
        this.createdAt = createdAt;
    }

    public static IssuedRefreshToken issue(UUID userId, Duration timeToLive) {
        String rawValue = generateRawValue();
        Instant now = Instant.now();
        RefreshToken token =
                new RefreshToken(
                        UUID.randomUUID(),
                        userId,
                        hash(rawValue),
                        now.plus(timeToLive),
                        false,
                        now);
        return new IssuedRefreshToken(token, rawValue);
    }

    public static RefreshToken reconstruct(
            UUID id,
            UUID userId,
            String tokenHash,
            Instant expiresAt,
            boolean revoked,
            Instant createdAt) {
        return new RefreshToken(id, userId, tokenHash, expiresAt, revoked, createdAt);
    }

    public static String hash(String rawValue) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed =
                    digest.digest(rawValue.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }

    private static String generateRawValue() {
        byte[] bytes = new byte[RAW_VALUE_BYTE_LENGTH];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public boolean isValid(Instant now) {
        return !revoked && now.isBefore(expiresAt);
    }

    public void revoke() {
        this.revoked = true;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public boolean isRevoked() {
        return revoked;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
