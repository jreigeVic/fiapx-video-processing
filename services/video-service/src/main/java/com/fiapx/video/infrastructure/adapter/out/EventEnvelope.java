package com.fiapx.video.infrastructure.adapter.out;

import java.time.Instant;
import java.util.UUID;

/** Matches the standard envelope in docs/LLD/shared-architecture.md. */
public record EventEnvelope<T>(
        UUID eventId,
        String eventType,
        Instant occurredAt,
        UUID correlationId,
        String producer,
        T payload) {}
