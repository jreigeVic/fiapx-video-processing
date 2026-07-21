package com.fiapx.video.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fiapx.video.application.dto.DownloadUrl;
import com.fiapx.video.application.ports.out.StoragePort;
import com.fiapx.video.application.ports.out.VideoRepositoryPort;
import com.fiapx.video.application.usecase.GenerateDownloadUrlUseCase;
import com.fiapx.video.domain.exception.VideoNotFoundException;
import com.fiapx.video.domain.exception.VideoNotReadyForDownloadException;
import com.fiapx.video.domain.model.StorageObjectKey;
import com.fiapx.video.domain.model.Video;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class GenerateDownloadUrlUseCaseTest {

    private final VideoRepositoryPort videoRepositoryPort = mock(VideoRepositoryPort.class);
    private final StoragePort storagePort = mock(StoragePort.class);
    private final GenerateDownloadUrlUseCase useCase =
            new GenerateDownloadUrlUseCase(
                    videoRepositoryPort, storagePort, Duration.ofMinutes(15));

    @Test
    void generatesUrlForDownloadableVideo() {
        UUID ownerId = UUID.randomUUID();
        UUID videoId = UUID.randomUUID();
        Video video =
                Video.receive(ownerId, "movie.mp4", StorageObjectKey.of("videos/original/x.mp4"));
        video.markProcessed(StorageObjectKey.of("videos/results/x.zip"));
        when(videoRepositoryPort.findByIdAndOwnerUserId(videoId, ownerId))
                .thenReturn(Optional.of(video));
        when(storagePort.generatePresignedDownloadUrl(
                        video.getResultObjectKey(), Duration.ofMinutes(15)))
                .thenReturn("https://example.com/signed");

        DownloadUrl result = useCase.execute(ownerId, videoId);

        assertThat(result.url()).isEqualTo("https://example.com/signed");
    }

    @Test
    void rejectsWhenVideoNotYetProcessed() {
        UUID ownerId = UUID.randomUUID();
        UUID videoId = UUID.randomUUID();
        Video video =
                Video.receive(ownerId, "movie.mp4", StorageObjectKey.of("videos/original/x.mp4"));
        when(videoRepositoryPort.findByIdAndOwnerUserId(videoId, ownerId))
                .thenReturn(Optional.of(video));

        assertThatThrownBy(() -> useCase.execute(ownerId, videoId))
                .isInstanceOf(VideoNotReadyForDownloadException.class);
    }

    @Test
    void rejectsWhenVideoNotFound() {
        UUID ownerId = UUID.randomUUID();
        UUID videoId = UUID.randomUUID();
        when(videoRepositoryPort.findByIdAndOwnerUserId(videoId, ownerId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(ownerId, videoId))
                .isInstanceOf(VideoNotFoundException.class);
    }
}
