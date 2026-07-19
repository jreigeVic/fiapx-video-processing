package com.fiapx.processing.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.fiapx.processing.application.ports.out.IdempotencyPort;
import com.fiapx.processing.domain.model.ProcessedEvent;
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
 * end to end, per docs/LLD/processing-worker.md's testing strategy. Requires a running Docker
 * daemon.
 */
@SpringBootTest
@Testcontainers
class JpaIdempotencyAdapterIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16")
                    .withDatabaseName("processing_db")
                    .withUsername("test")
                    .withPassword("test");

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired private IdempotencyPort idempotencyPort;

    @Test
    void recordsAndDetectsProcessedEvent() {
        UUID eventId = UUID.randomUUID();

        assertThat(idempotencyPort.existsByEventId(eventId)).isFalse();

        idempotencyPort.save(ProcessedEvent.record(eventId, "VideoUploaded"));

        assertThat(idempotencyPort.existsByEventId(eventId)).isTrue();
    }
}
