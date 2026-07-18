package com.fiapx.video.domain.model;

import java.time.Instant;
import java.util.UUID;

// Domain entity placeholder (epic 004 fills in getters/business rules).
@SuppressWarnings("PMD.UnusedPrivateField")
public class Video {
    private UUID id;
    private UUID ownerUserId;
    private String originalFileName;
    private String sourceObjectKey;
    private String resultObjectKey;
    private String status;
    private String failureReason;
    private Instant createdAt;
    private Instant updatedAt;
}
