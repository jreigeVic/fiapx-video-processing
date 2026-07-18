package com.fiapx.processing.infrastructure.adapter.out;

import java.util.UUID;

public record VideoFailedPayload(
        UUID videoId, UUID ownerUserId, String ownerEmail, String failureReason) {}
