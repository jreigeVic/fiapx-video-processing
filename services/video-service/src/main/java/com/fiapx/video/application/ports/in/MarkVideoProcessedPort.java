package com.fiapx.video.application.ports.in;

import com.fiapx.video.domain.model.StorageObjectKey;
import java.util.UUID;

public interface MarkVideoProcessedPort {

    void execute(UUID eventId, UUID videoId, StorageObjectKey resultObjectKey);
}
