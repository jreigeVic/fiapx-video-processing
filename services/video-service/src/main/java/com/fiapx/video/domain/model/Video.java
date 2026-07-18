package com.fiapx.video.domain.model;

import java.time.Instant;
import java.util.UUID;

public final class Video {

    private final UUID id;
    private final UUID ownerUserId;
    private final String originalFileName;
    private final StorageObjectKey sourceObjectKey;
    private StorageObjectKey resultObjectKey;
    private VideoStatus status;
    private String failureReason;
    private final Instant createdAt;
    private Instant updatedAt;

    private Video(
            UUID id,
            UUID ownerUserId,
            String originalFileName,
            StorageObjectKey sourceObjectKey,
            StorageObjectKey resultObjectKey,
            VideoStatus status,
            String failureReason,
            Instant createdAt,
            Instant updatedAt) {
        this.id = id;
        this.ownerUserId = ownerUserId;
        this.originalFileName = originalFileName;
        this.sourceObjectKey = sourceObjectKey;
        this.resultObjectKey = resultObjectKey;
        this.status = status;
        this.failureReason = failureReason;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Video receive(
            UUID ownerUserId, String originalFileName, StorageObjectKey sourceObjectKey) {
        Instant now = Instant.now();
        return new Video(
                UUID.randomUUID(),
                ownerUserId,
                originalFileName,
                sourceObjectKey,
                null,
                VideoStatus.RECEIVED,
                null,
                now,
                now);
    }

    public static Video reconstruct(
            UUID id,
            UUID ownerUserId,
            String originalFileName,
            StorageObjectKey sourceObjectKey,
            StorageObjectKey resultObjectKey,
            VideoStatus status,
            String failureReason,
            Instant createdAt,
            Instant updatedAt) {
        return new Video(
                id,
                ownerUserId,
                originalFileName,
                sourceObjectKey,
                resultObjectKey,
                status,
                failureReason,
                createdAt,
                updatedAt);
    }

    public void markProcessing() {
        this.status = VideoStatus.PROCESSING;
        this.updatedAt = Instant.now();
    }

    public void markProcessed(StorageObjectKey resultObjectKey) {
        this.resultObjectKey = resultObjectKey;
        this.status = VideoStatus.PROCESSED;
        this.failureReason = null;
        this.updatedAt = Instant.now();
    }

    public void markFailed(String failureReason) {
        this.status = VideoStatus.FAILED;
        this.failureReason = failureReason;
        this.updatedAt = Instant.now();
    }

    public boolean isOwnedBy(UUID userId) {
        return ownerUserId.equals(userId);
    }

    public boolean isDownloadable() {
        return status == VideoStatus.PROCESSED && resultObjectKey != null;
    }

    public UUID getId() {
        return id;
    }

    public UUID getOwnerUserId() {
        return ownerUserId;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public StorageObjectKey getSourceObjectKey() {
        return sourceObjectKey;
    }

    public StorageObjectKey getResultObjectKey() {
        return resultObjectKey;
    }

    public VideoStatus getStatus() {
        return status;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
