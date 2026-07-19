package com.fiapx.video.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.fiapx.video.application.ports.out.VideoRepositoryPort;
import com.fiapx.video.domain.model.StorageObjectKey;
import com.fiapx.video.domain.model.Video;
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
 * end to end, per docs/LLD/video-service.md's testing strategy. Requires a running Docker daemon.
 */
@SpringBootTest
@Testcontainers
class JpaVideoRepositoryAdapterIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16")
                    .withDatabaseName("video_db")
                    .withUsername("test")
                    .withPassword("test");

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired private VideoRepositoryPort videoRepositoryPort;

    @Test
    void persistsAndRetrievesVideoByOwner() {
        UUID ownerUserId = UUID.randomUUID();
        Video video =
                Video.receive(
                        ownerUserId, "movie.mp4", StorageObjectKey.of("videos/original/x.mp4"));

        Video saved = videoRepositoryPort.save(video);

        assertThat(videoRepositoryPort.findById(saved.getId())).isPresent();
        assertThat(videoRepositoryPort.findByIdAndOwnerUserId(saved.getId(), ownerUserId))
                .isPresent();
        assertThat(videoRepositoryPort.findByOwnerUserId(ownerUserId)).hasSize(1);
    }
}
