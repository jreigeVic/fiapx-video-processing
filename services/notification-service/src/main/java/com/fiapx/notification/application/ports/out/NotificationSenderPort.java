package com.fiapx.notification.application.ports.out;

import java.util.UUID;

public interface NotificationSenderPort {

    void sendVideoProcessed(
            String recipientEmail, UUID videoId, String resultObjectKey, int frameCount);

    void sendVideoFailed(String recipientEmail, UUID videoId, String failureReason);
}
