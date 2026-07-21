package com.fiapx.notification.domain.model;

import java.time.Instant;
import java.util.UUID;

public final class Notification {

    private final UUID id;
    private final UUID videoId;
    private final UUID ownerUserId;
    private final NotificationType type;
    private NotificationStatus status;
    private final Instant createdAt;
    private Instant sentAt;

    private Notification(
            UUID id,
            UUID videoId,
            UUID ownerUserId,
            NotificationType type,
            NotificationStatus status,
            Instant createdAt,
            Instant sentAt) {
        this.id = id;
        this.videoId = videoId;
        this.ownerUserId = ownerUserId;
        this.type = type;
        this.status = status;
        this.createdAt = createdAt;
        this.sentAt = sentAt;
    }

    public static Notification create(UUID videoId, UUID ownerUserId, NotificationType type) {
        return new Notification(
                UUID.randomUUID(),
                videoId,
                ownerUserId,
                type,
                NotificationStatus.PENDING,
                Instant.now(),
                null);
    }

    public static Notification reconstruct(
            UUID id,
            UUID videoId,
            UUID ownerUserId,
            NotificationType type,
            NotificationStatus status,
            Instant createdAt,
            Instant sentAt) {
        return new Notification(id, videoId, ownerUserId, type, status, createdAt, sentAt);
    }

    public void markSent() {
        this.status = NotificationStatus.SENT;
        this.sentAt = Instant.now();
    }

    public void markFailed() {
        this.status = NotificationStatus.FAILED;
    }

    public UUID getId() {
        return id;
    }

    public UUID getVideoId() {
        return videoId;
    }

    public UUID getOwnerUserId() {
        return ownerUserId;
    }

    public NotificationType getType() {
        return type;
    }

    public NotificationStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getSentAt() {
        return sentAt;
    }
}
