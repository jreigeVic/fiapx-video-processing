package com.fiapx.processing.infrastructure.adapter.out;

import java.util.UUID;

public record VideoProcessedPayload(
        UUID videoId,
        UUID ownerUserId,
        String ownerEmail,
        String resultObjectKey,
        int frameCount) {}
