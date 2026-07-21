package com.fiapx.video.domain.model;

import java.util.Objects;

public final class StorageObjectKey {

    private final String value;

    private StorageObjectKey(String value) {
        this.value = value;
    }

    public static StorageObjectKey of(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("StorageObjectKey must not be blank");
        }
        return new StorageObjectKey(value);
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        return this == other
                || (other instanceof StorageObjectKey that && value.equals(that.value));
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
