package com.fiapx.notification.application.ports.out;

import com.fiapx.notification.domain.model.ProcessedEvent;
import java.util.UUID;

public interface NotificationIdempotencyPort {

    boolean existsByEventId(UUID eventId);

    void save(ProcessedEvent processedEvent);
}
