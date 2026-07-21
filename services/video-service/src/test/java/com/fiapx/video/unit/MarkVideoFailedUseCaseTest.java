package com.fiapx.video.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fiapx.video.application.ports.out.ProcessedEventIdempotencyPort;
import com.fiapx.video.application.ports.out.VideoRepositoryPort;
import com.fiapx.video.application.usecase.MarkVideoFailedUseCase;
import com.fiapx.video.domain.model.StorageObjectKey;
import com.fiapx.video.domain.model.Video;
import com.fiapx.video.domain.model.VideoStatus;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class MarkVideoFailedUseCaseTest {

    private final VideoRepositoryPort videoRepositoryPort = mock(VideoRepositoryPort.class);
    private final ProcessedEventIdempotencyPort idempotencyPort =
            mock(ProcessedEventIdempotencyPort.class);
    private final MarkVideoFailedUseCase useCase =
            new MarkVideoFailedUseCase(videoRepositoryPort, idempotencyPort);

    @Test
    void marksVideoAsFailed() {
        UUID videoId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        Video video =
                Video.receive(
                        UUID.randomUUID(),
                        "movie.mp4",
                        StorageObjectKey.of("videos/original/x.mp4"));
        when(idempotencyPort.existsByEventId(eventId)).thenReturn(false);
        when(videoRepositoryPort.findById(videoId)).thenReturn(Optional.of(video));

        useCase.execute(eventId, videoId, "PROCESSING_ERROR");

        assertThat(video.getStatus()).isEqualTo(VideoStatus.FAILED);
        assertThat(video.getFailureReason()).isEqualTo("PROCESSING_ERROR");
        verify(videoRepositoryPort).save(video);
        verify(idempotencyPort).save(any());
    }

    @Test
    void ignoresDuplicateEvent() {
        UUID eventId = UUID.randomUUID();
        when(idempotencyPort.existsByEventId(eventId)).thenReturn(true);

        useCase.execute(eventId, UUID.randomUUID(), "PROCESSING_ERROR");

        verify(videoRepositoryPort, never()).findById(any());
    }
}
