package com.fiapx.video.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fiapx.video.application.ports.out.VideoRepositoryPort;
import com.fiapx.video.application.usecase.ListUserVideosUseCase;
import com.fiapx.video.domain.model.StorageObjectKey;
import com.fiapx.video.domain.model.Video;
import com.fiapx.video.domain.model.VideoStatus;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ListUserVideosUseCaseTest {

    private final VideoRepositoryPort videoRepositoryPort = mock(VideoRepositoryPort.class);
    private final ListUserVideosUseCase useCase = new ListUserVideosUseCase(videoRepositoryPort);

    @Test
    void filtersByStatusWhenProvided() {
        UUID ownerId = UUID.randomUUID();
        Video received =
                Video.receive(ownerId, "a.mp4", StorageObjectKey.of("videos/original/a.mp4"));
        Video processing =
                Video.receive(ownerId, "b.mp4", StorageObjectKey.of("videos/original/b.mp4"));
        processing.markProcessing();
        when(videoRepositoryPort.findByOwnerUserId(ownerId))
                .thenReturn(List.of(received, processing));

        List<Video> result = useCase.execute(ownerId, VideoStatus.PROCESSING);

        assertThat(result).containsExactly(processing);
    }

    @Test
    void returnsAllWhenNoFilter() {
        UUID ownerId = UUID.randomUUID();
        Video received =
                Video.receive(ownerId, "a.mp4", StorageObjectKey.of("videos/original/a.mp4"));
        when(videoRepositoryPort.findByOwnerUserId(ownerId)).thenReturn(List.of(received));

        assertThat(useCase.execute(ownerId, null)).containsExactly(received);
    }
}
