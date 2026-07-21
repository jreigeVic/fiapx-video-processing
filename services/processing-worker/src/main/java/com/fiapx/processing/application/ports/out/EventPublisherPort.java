package com.fiapx.processing.application.ports.out;

import com.fiapx.processing.domain.model.FailureReason;
import com.fiapx.processing.domain.model.FrameCount;
import com.fiapx.processing.domain.model.StorageObjectKey;
import java.util.UUID;

public interface EventPublisherPort {

    void publishVideoProcessed(
            UUID videoId,
            UUID ownerUserId,
            String ownerEmail,
            StorageObjectKey resultObjectKey,
            FrameCount frameCount);

    void publishVideoFailed(
            UUID videoId, UUID ownerUserId, String ownerEmail, FailureReason failureReason);
}
