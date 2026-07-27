package com.fiapx.notification.application.ports.in;

import java.util.UUID;

public interface NotifyVideoProcessedPort {

    void execute(
            UUID eventId,
            UUID videoId,
            UUID ownerUserId,
            String ownerEmail,
            String resultObjectKey,
            int frameCount);
}
