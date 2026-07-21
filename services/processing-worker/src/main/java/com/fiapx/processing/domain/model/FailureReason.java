package com.fiapx.processing.domain.model;

/** Wraps a safe, user-facing failure message - never a raw exception message or stack trace. */
public final class FailureReason {

    private final String value;

    private FailureReason(String value) {
        this.value = value;
    }

    public static FailureReason of(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("FailureReason must not be blank");
        }
        return new FailureReason(value);
    }

    public String value() {
        return value;
    }
}
