package com.fiapx.processing.application.ports.out;

import com.fiapx.processing.domain.model.ProcessedEvent;
import java.util.UUID;

public interface IdempotencyPort {

    boolean existsByEventId(UUID eventId);

    void save(ProcessedEvent processedEvent);
}
