package com.fiapx.processing.domain.model;

public final class FrameCount {

    private final int value;

    private FrameCount(int value) {
        this.value = value;
    }

    public static FrameCount of(int value) {
        if (value < 0) {
            throw new IllegalArgumentException("FrameCount must not be negative");
        }
        return new FrameCount(value);
    }

    public int value() {
        return value;
    }
}
