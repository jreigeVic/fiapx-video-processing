package com.fiapx.processing.application.ports.in;

import com.fiapx.processing.domain.model.StorageObjectKey;
import java.util.UUID;

public interface ProcessUploadedVideoPort {

    void execute(
            UUID eventId,
            UUID videoId,
            UUID ownerUserId,
            String ownerEmail,
            StorageObjectKey sourceObjectKey);
}
