package com.fiapx.video.application.ports.out;

import com.fiapx.video.domain.model.ProcessedEvent;
import java.util.UUID;

public interface ProcessedEventIdempotencyPort {

    boolean existsByEventId(UUID eventId);

    void save(ProcessedEvent processedEvent);
}
