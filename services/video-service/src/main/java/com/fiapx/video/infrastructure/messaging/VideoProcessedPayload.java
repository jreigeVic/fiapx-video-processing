package com.fiapx.video.infrastructure.messaging;

import java.util.UUID;

public record VideoProcessedPayload(
        UUID videoId,
        UUID ownerUserId,
        String ownerEmail,
        String resultObjectKey,
        int frameCount) {}
