package com.fiapx.processing.domain.model;

import java.util.UUID;

/** In-flight processing state for a single VideoUploaded event - never persisted (see LLD). */
public final class ProcessingJob {

    private final UUID videoId;
    private final UUID ownerUserId;
    private final StorageObjectKey sourceObjectKey;
    private StorageObjectKey resultObjectKey;
    private ProcessingStatus status;

    private ProcessingJob(UUID videoId, UUID ownerUserId, StorageObjectKey sourceObjectKey) {
        this.videoId = videoId;
        this.ownerUserId = ownerUserId;
        this.sourceObjectKey = sourceObjectKey;
        this.status = ProcessingStatus.PROCESSING;
    }

    public static ProcessingJob start(
            UUID videoId, UUID ownerUserId, StorageObjectKey sourceObjectKey) {
        return new ProcessingJob(videoId, ownerUserId, sourceObjectKey);
    }

    public void succeed(StorageObjectKey resultObjectKey) {
        this.resultObjectKey = resultObjectKey;
        this.status = ProcessingStatus.SUCCEEDED;
    }

    public void fail() {
        this.status = ProcessingStatus.FAILED;
    }

    public UUID getVideoId() {
        return videoId;
    }

    public UUID getOwnerUserId() {
        return ownerUserId;
    }

    public StorageObjectKey getSourceObjectKey() {
        return sourceObjectKey;
    }

    public StorageObjectKey getResultObjectKey() {
        return resultObjectKey;
    }

    public ProcessingStatus getStatus() {
        return status;
    }
}
