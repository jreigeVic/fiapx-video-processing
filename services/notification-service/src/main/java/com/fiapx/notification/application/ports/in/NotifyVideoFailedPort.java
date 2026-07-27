package com.fiapx.notification.application.ports.in;

import java.util.UUID;

public interface NotifyVideoFailedPort {

    void execute(
            UUID eventId, UUID videoId, UUID ownerUserId, String ownerEmail, String failureReason);
}
