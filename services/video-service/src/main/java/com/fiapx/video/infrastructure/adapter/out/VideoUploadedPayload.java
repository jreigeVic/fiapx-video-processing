package com.fiapx.video.infrastructure.adapter.out;

import java.util.UUID;

/**
 * Matches docs/diagrams/event-catalog.md's VideoUploaded payload, plus ownerEmail: Notification
 * Service cannot reach auth_db/video_db (LLD boundary), so the recipient address is threaded
 * through the event chain from the JWT's own email claim, captured once at upload time.
 */
public record VideoUploadedPayload(
        UUID videoId,
        UUID ownerUserId,
        String ownerEmail,
        String originalFileName,
        String sourceObjectKey) {}
