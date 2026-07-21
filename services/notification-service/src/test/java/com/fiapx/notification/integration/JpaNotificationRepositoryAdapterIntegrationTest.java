package com.fiapx.notification.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.fiapx.notification.application.ports.out.NotificationIdempotencyPort;
import com.fiapx.notification.application.ports.out.NotificationRepositoryPort;
import com.fiapx.notification.domain.model.Notification;
import com.fiapx.notification.domain.model.NotificationType;
import com.fiapx.notification.domain.model.ProcessedEvent;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Runs the real Flyway migration against Postgres 16 to validate the hexagonal persistence adapter
 * end to end, per docs/LLD/notification-service.md's testing strategy. Requires a running Docker
 * daemon.
 */
@SpringBootTest
@Testcontainers
class JpaNotificationRepositoryAdapterIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16")
                    .withDatabaseName("notification_db")
                    .withUsername("test")
                    .withPassword("test");

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired private NotificationRepositoryPort notificationRepositoryPort;
    @Autowired private NotificationIdempotencyPort notificationIdempotencyPort;

    // NotificationRepositoryPort only exposes save() (the application never
    // re-reads a notification once written), so this can't assert a
    // read-back without reaching for the infrastructure-layer JPA
    // repository directly, which would violate the hexagonal boundary
    // (see HexagonalArchitectureTest). Exercising save() against the real
    // Flyway-migrated schema is still the point: it catches entity/column
    // mapping mismatches that an H2/unit test would miss.
    @Test
    void persistsNotificationWithoutError() {
        Notification notification =
                Notification.create(
                        UUID.randomUUID(), UUID.randomUUID(), NotificationType.VIDEO_PROCESSED);
        notification.markSent();

        notificationRepositoryPort.save(notification);
    }

    @Test
    void recordsAndDetectsProcessedEvent() {
        UUID eventId = UUID.randomUUID();

        assertThat(notificationIdempotencyPort.existsByEventId(eventId)).isFalse();

        notificationIdempotencyPort.save(ProcessedEvent.record(eventId, "VideoProcessed"));

        assertThat(notificationIdempotencyPort.existsByEventId(eventId)).isTrue();
    }
}
