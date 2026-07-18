package com.fiapx.video.infrastructure.adapter.out;

import com.fiapx.video.application.ports.out.ProcessedEventIdempotencyPort;
import com.fiapx.video.domain.model.ProcessedEvent;
import com.fiapx.video.infrastructure.repository.ProcessedEventJpaEntity;
import com.fiapx.video.infrastructure.repository.ProcessedEventJpaRepository;
import java.util.UUID;

public class JpaProcessedEventIdempotencyAdapter implements ProcessedEventIdempotencyPort {

    private final ProcessedEventJpaRepository jpaRepository;

    public JpaProcessedEventIdempotencyAdapter(ProcessedEventJpaRepository jpaRepository) {
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
