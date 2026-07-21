package com.fiapx.video.infrastructure.messaging;

import java.util.UUID;

public record VideoFailedPayload(
        UUID videoId, UUID ownerUserId, String ownerEmail, String failureReason) {}
