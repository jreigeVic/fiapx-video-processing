package com.fiapx.video.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fiapx.video.application.ports.out.VideoRepositoryPort;
import com.fiapx.video.application.usecase.GetVideoUseCase;
import com.fiapx.video.domain.exception.VideoNotFoundException;
import com.fiapx.video.domain.model.StorageObjectKey;
import com.fiapx.video.domain.model.Video;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class GetVideoUseCaseTest {

    private final VideoRepositoryPort videoRepositoryPort = mock(VideoRepositoryPort.class);
    private final GetVideoUseCase useCase = new GetVideoUseCase(videoRepositoryPort);

    @Test
    void returnsVideoWhenOwnedByUser() {
        UUID ownerId = UUID.randomUUID();
        UUID videoId = UUID.randomUUID();
        Video video = Video.receive(ownerId, "a.mp4", StorageObjectKey.of("videos/original/a.mp4"));
        when(videoRepositoryPort.findByIdAndOwnerUserId(videoId, ownerId))
                .thenReturn(Optional.of(video));

        assertThat(useCase.execute(ownerId, videoId)).isEqualTo(video);
    }

    @Test
    void throwsWhenNotFoundForUser() {
        UUID ownerId = UUID.randomUUID();
        UUID videoId = UUID.randomUUID();
        when(videoRepositoryPort.findByIdAndOwnerUserId(videoId, ownerId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(ownerId, videoId))
                .isInstanceOf(VideoNotFoundException.class);
    }
}
