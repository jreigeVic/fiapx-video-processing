package com.fiapx.notification.infrastructure.adapter.out;

import com.fiapx.notification.application.ports.out.NotificationIdempotencyPort;
import com.fiapx.notification.domain.model.ProcessedEvent;
import com.fiapx.notification.infrastructure.repository.ProcessedEventJpaEntity;
import com.fiapx.notification.infrastructure.repository.ProcessedEventJpaRepository;
import java.util.UUID;

public class JpaNotificationIdempotencyAdapter implements NotificationIdempotencyPort {

    private final ProcessedEventJpaRepository jpaRepository;

    public JpaNotificationIdempotencyAdapter(ProcessedEventJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public boolean existsByEventId(UUID eventId) {
        return jpaRepository.existsById(eventId);
    }

    @Override
    public void save(ProcessedEvent processedEvent) {
        jpaRepository.save(
                new ProcessedEventJpaEntity(
                        processedEvent.getEventId(),
                        processedEvent.getEventType(),
                        processedEvent.getProcessedAt()));
    }
}
