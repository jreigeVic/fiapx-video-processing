package com.fiapx.notification.infrastructure.adapter.in.messaging;

import java.util.UUID;

public record VideoFailedPayload(
        UUID videoId, UUID ownerUserId, String ownerEmail, String failureReason) {}
