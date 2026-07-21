package com.fiapx.notification.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fiapx.notification.application.ports.out.NotificationIdempotencyPort;
import com.fiapx.notification.application.ports.out.NotificationRepositoryPort;
import com.fiapx.notification.application.ports.out.NotificationSenderPort;
import com.fiapx.notification.application.usecase.NotifyVideoFailedUseCase;
import com.fiapx.notification.domain.exception.NotificationDeliveryException;
import com.fiapx.notification.domain.model.Notification;
import com.fiapx.notification.domain.model.NotificationStatus;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class NotifyVideoFailedUseCaseTest {

    private final NotificationSenderPort notificationSenderPort =
            mock(NotificationSenderPort.class);
    private final NotificationRepositoryPort notificationRepositoryPort =
            mock(NotificationRepositoryPort.class);
    private final NotificationIdempotencyPort notificationIdempotencyPort =
            mock(NotificationIdempotencyPort.class);
    private final NotifyVideoFailedUseCase useCase =
            new NotifyVideoFailedUseCase(
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
                "PROCESSING_ERROR");

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
                .sendVideoFailed(any(), any(), any());

        useCase.execute(
                eventId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                "owner@user.com",
                "PROCESSING_ERROR");

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
                .sendVideoFailed(any(), any(), any());

        assertThatThrownBy(
                        () ->
                                useCase.execute(
                                        eventId,
                                        UUID.randomUUID(),
                                        UUID.randomUUID(),
                                        "owner@user.com",
                                        "PROCESSING_ERROR"))
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
                "PROCESSING_ERROR");

        verify(notificationSenderPort, never()).sendVideoFailed(any(), any(), any());
        verify(notificationRepositoryPort, never()).save(any());
    }
}
