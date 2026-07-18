package com.fiapx.notification.infrastructure.adapter.in.messaging;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record InboundEventEnvelope(UUID eventId, String eventType, JsonNode payload) {}
