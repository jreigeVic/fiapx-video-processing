package com.fiapx.notification.unit;

import static org.assertj.core.api.Assertions.assertThat;

import com.fiapx.notification.domain.model.Notification;
import com.fiapx.notification.domain.model.NotificationStatus;
import com.fiapx.notification.domain.model.NotificationType;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class NotificationTest {

    @Test
    void startsAsPending() {
        Notification notification =
                Notification.create(
                        UUID.randomUUID(), UUID.randomUUID(), NotificationType.VIDEO_PROCESSED);
        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.PENDING);
        assertThat(notification.getSentAt()).isNull();
    }

    @Test
    void markSentSetsStatusAndTimestamp() {
        Notification notification =
                Notification.create(
                        UUID.randomUUID(), UUID.randomUUID(), NotificationType.VIDEO_FAILED);

        notification.markSent();

        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.SENT);
        assertThat(notification.getSentAt()).isNotNull();
    }

    @Test
    void markFailedSetsStatus() {
        Notification notification =
                Notification.create(
                        UUID.randomUUID(), UUID.randomUUID(), NotificationType.VIDEO_PROCESSED);

        notification.markFailed();

        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.FAILED);
    }
}
