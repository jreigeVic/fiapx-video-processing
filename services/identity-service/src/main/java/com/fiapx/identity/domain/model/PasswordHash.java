package com.fiapx.identity.domain.model;

import java.util.Objects;

public final class PasswordHash {

    private final String value;

    private PasswordHash(String value) {
        this.value = value;
    }

    public static PasswordHash fromHash(String alreadyHashedValue) {
        if (alreadyHashedValue == null || alreadyHashedValue.isBlank()) {
            throw new IllegalArgumentException("PasswordHash must not be blank");
        }
        return new PasswordHash(alreadyHashedValue);
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        return this == other || (other instanceof PasswordHash that && value.equals(that.value));
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
