package com.fiapx.video.infrastructure.repository;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "videos")
public class VideoJpaEntity {

    @Id private UUID id;

    @Column(name = "owner_user_id", nullable = false)
    private UUID ownerUserId;

    @Column(name = "original_file_name", nullable = false)
    private String originalFileName;

    @Column(name = "source_object_key", nullable = false)
    private String sourceObjectKey;

    @Column(name = "result_object_key")
    private String resultObjectKey;

    @Column(nullable = false)
    private String status;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected VideoJpaEntity() {}

    public VideoJpaEntity(
            UUID id,
            UUID ownerUserId,
            String originalFileName,
            String sourceObjectKey,
            String resultObjectKey,
            String status,
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

    public UUID getId() {
        return id;
    }

    public UUID getOwnerUserId() {
        return ownerUserId;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public String getSourceObjectKey() {
        return sourceObjectKey;
    }

    public String getResultObjectKey() {
        return resultObjectKey;
    }

    public String getStatus() {
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
