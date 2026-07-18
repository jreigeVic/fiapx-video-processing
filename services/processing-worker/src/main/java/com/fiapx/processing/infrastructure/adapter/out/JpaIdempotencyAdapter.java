package com.fiapx.processing.infrastructure.adapter.out;

import com.fiapx.processing.application.ports.out.IdempotencyPort;
import com.fiapx.processing.domain.model.ProcessedEvent;
import com.fiapx.processing.infrastructure.repository.ProcessedEventJpaEntity;
import com.fiapx.processing.infrastructure.repository.ProcessedEventJpaRepository;
import java.util.UUID;

public class JpaIdempotencyAdapter implements IdempotencyPort {

    private final ProcessedEventJpaRepository jpaRepository;

    public JpaIdempotencyAdapter(ProcessedEventJpaRepository jpaRepository) {
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
