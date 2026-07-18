package com.fiapx.notification.application.usecase;

import com.fiapx.notification.application.ports.out.NotificationIdempotencyPort;
import com.fiapx.notification.application.ports.out.NotificationRepositoryPort;
import com.fiapx.notification.application.ports.out.NotificationSenderPort;
import com.fiapx.notification.domain.exception.NotificationDeliveryException;
import com.fiapx.notification.domain.model.Notification;
import com.fiapx.notification.domain.model.NotificationType;
import com.fiapx.notification.domain.model.ProcessedEvent;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link NotificationDeliveryException} means the send permanently failed (e.g. rejected
 * recipient) - the notification is recorded as FAILED and the event is acknowledged as handled. Any
 * other exception is left to propagate so the SQS consumer does not ack the message, letting SQS
 * redrive/DLQ handle the retry.
 */
public class NotifyVideoProcessedUseCase {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotifyVideoProcessedUseCase.class);

    private final NotificationSenderPort notificationSenderPort;
    private final NotificationRepositoryPort notificationRepositoryPort;
    private final NotificationIdempotencyPort notificationIdempotencyPort;

    public NotifyVideoProcessedUseCase(
            NotificationSenderPort notificationSenderPort,
            NotificationRepositoryPort notificationRepositoryPort,
            NotificationIdempotencyPort notificationIdempotencyPort) {
        this.notificationSenderPort = notificationSenderPort;
        this.notificationRepositoryPort = notificationRepositoryPort;
        this.notificationIdempotencyPort = notificationIdempotencyPort;
    }

    public void execute(
            UUID eventId,
            UUID videoId,
            UUID ownerUserId,
            String ownerEmail,
            String resultObjectKey,
            int frameCount) {
        if (notificationIdempotencyPort.existsByEventId(eventId)) {
            return;
        }

        Notification notification =
                Notification.create(videoId, ownerUserId, NotificationType.VIDEO_PROCESSED);
        try {
            notificationSenderPort.sendVideoProcessed(
                    ownerEmail, videoId, resultObjectKey, frameCount);
            notification.markSent();
        } catch (NotificationDeliveryException e) {
            LOGGER.warn("Permanent delivery failure for video {}: {}", videoId, e.getMessage());
            notification.markFailed();
        }

        notificationRepositoryPort.save(notification);
        notificationIdempotencyPort.save(ProcessedEvent.record(eventId, "VideoProcessed"));
    }
}
