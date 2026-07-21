package com.fiapx.processing.infrastructure.adapter.in.messaging;

import java.util.UUID;

public record VideoUploadedPayload(
        UUID videoId,
        UUID ownerUserId,
        String ownerEmail,
        String originalFileName,
        String sourceObjectKey) {}
