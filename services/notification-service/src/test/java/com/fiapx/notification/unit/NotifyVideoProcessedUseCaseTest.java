package com.fiapx.notification.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fiapx.notification.application.ports.out.NotificationIdempotencyPort;
import com.fiapx.notification.application.ports.out.NotificationRepositoryPort;
import com.fiapx.notification.application.ports.out.NotificationSenderPort;
import com.fiapx.notification.application.usecase.NotifyVideoProcessedUseCase;
import com.fiapx.notification.domain.exception.NotificationDeliveryException;
import com.fiapx.notification.domain.model.Notification;
import com.fiapx.notification.domain.model.NotificationStatus;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class NotifyVideoProcessedUseCaseTest {

    private final NotificationSenderPort notificationSenderPort =
            mock(NotificationSenderPort.class);
    private final NotificationRepositoryPort notificationRepositoryPort =
            mock(NotificationRepositoryPort.class);
    private final NotificationIdempotencyPort notificationIdempotencyPort =
            mock(NotificationIdempotencyPort.class);
    private final NotifyVideoProcessedUseCase useCase =
            new NotifyVideoProcessedUseCase(
                    notificationSenderPort,
                    notificationRepositoryPort,
                    notificationIdempotencyPort);

    @Test
    void marksNotificationSentOnSuccess() {
        UUID eventId = UUID.randomUUID();
        when(notificationIdempotencyPort.existsByEventId(eventId)).thenReturn(false);

        useCase.execute(
                eventId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                "owner@user.com",
                "videos/results/x.zip",
                42);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepositoryPort).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(NotificationStatus.SENT);
        verify(notificationIdempotencyPort).save(any());
    }

    @Test
    void marksNotificationFailedOnPermanentDeliveryFailure() {
        UUID eventId = UUID.randomUUID();
        when(notificationIdempotencyPort.existsByEventId(eventId)).thenReturn(false);
        doThrow(new NotificationDeliveryException("RECIPIENT_REJECTED"))
                .when(notificationSenderPort)
                .sendVideoProcessed(any(), any(), any(), eq(42));

        useCase.execute(
                eventId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                "owner@user.com",
                "videos/results/x.zip",
                42);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepositoryPort).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(NotificationStatus.FAILED);
        verify(notificationIdempotencyPort).save(any());
    }

    @Test
    void propagatesUnexpectedFailureWithoutSavingIdempotency() {
        UUID eventId = UUID.randomUUID();
        when(notificationIdempotencyPort.existsByEventId(eventId)).thenReturn(false);
        doThrow(new RuntimeException("SES unavailable"))
                .when(notificationSenderPort)
                .sendVideoProcessed(any(), any(), any(), eq(42));

        assertThatThrownBy(
                        () ->
                                useCase.execute(
                                        eventId,
                                        UUID.randomUUID(),
                                        UUID.randomUUID(),
                                        "owner@user.com",
                                        "videos/results/x.zip",
                                        42))
                .isInstanceOf(RuntimeException.class);

        verify(notificationIdempotencyPort, never()).save(any());
    }

    @Test
    void ignoresDuplicateEvent() {
        UUID eventId = UUID.randomUUID();
        when(notificationIdempotencyPort.existsByEventId(eventId)).thenReturn(true);

        useCase.execute(
                eventId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                "owner@user.com",
                "videos/results/x.zip",
                42);

        verify(notificationSenderPort, never()).sendVideoProcessed(any(), any(), any(), anyInt());
        verify(notificationRepositoryPort, never()).save(any());
    }
}
