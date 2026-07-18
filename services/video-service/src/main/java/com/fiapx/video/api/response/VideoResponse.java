package com.fiapx.video.api.response;

import com.fiapx.video.domain.model.VideoStatus;
import java.time.Instant;
import java.util.UUID;

public record VideoResponse(
        UUID id,
        String originalFileName,
        VideoStatus status,
        Instant createdAt,
        Instant updatedAt,
        boolean downloadAvailable) {}
