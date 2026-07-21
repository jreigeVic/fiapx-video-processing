package com.fiapx.video.domain.model;

import java.time.Instant;
import java.util.UUID;

public final class ProcessedEvent {

    private final UUID eventId;
    private final String eventType;
    private final Instant processedAt;

    private ProcessedEvent(UUID eventId, String eventType, Instant processedAt) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.processedAt = processedAt;
    }

    public static ProcessedEvent record(UUID eventId, String eventType) {
        return new ProcessedEvent(eventId, eventType, Instant.now());
    }

    public UUID getEventId() {
        return eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }
}
