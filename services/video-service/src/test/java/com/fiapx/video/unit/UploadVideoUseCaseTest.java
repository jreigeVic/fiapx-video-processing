package com.fiapx.video.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fiapx.video.application.dto.UploadedFile;
import com.fiapx.video.application.ports.out.EventPublisherPort;
import com.fiapx.video.application.ports.out.StoragePort;
import com.fiapx.video.application.ports.out.VideoRepositoryPort;
import com.fiapx.video.application.usecase.UploadVideoUseCase;
import com.fiapx.video.domain.exception.InvalidUploadException;
import com.fiapx.video.domain.model.Video;
import com.fiapx.video.domain.model.VideoStatus;
import java.io.ByteArrayInputStream;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class UploadVideoUseCaseTest {

    private final VideoRepositoryPort videoRepositoryPort = mock(VideoRepositoryPort.class);
    private final StoragePort storagePort = mock(StoragePort.class);
    private final EventPublisherPort eventPublisherPort = mock(EventPublisherPort.class);
    private final UploadVideoUseCase useCase =
            new UploadVideoUseCase(videoRepositoryPort, storagePort, eventPublisherPort);

    @Test
    void uploadsFileAndPublishesEvent() {
        when(videoRepositoryPort.save(any(Video.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        UploadedFile file =
                new UploadedFile(
                        "movie.mp4",
                        "video/mp4",
                        10,
                        new ByteArrayInputStream(new byte[] {1, 2, 3}));

        Video result = useCase.execute(UUID.randomUUID(), "owner@user.com", file);

        assertThat(result.getStatus()).isEqualTo(VideoStatus.PROCESSING);
        verify(storagePort)
                .store(
                        any(),
                        any(),
                        org.mockito.ArgumentMatchers.eq(10L),
                        org.mockito.ArgumentMatchers.eq("video/mp4"));
        verify(eventPublisherPort)
                .publishVideoUploaded(
                        any(Video.class), org.mockito.ArgumentMatchers.eq("owner@user.com"));
        verify(videoRepositoryPort, times(2)).save(any(Video.class));
    }

    @Test
    void rejectsEmptyFile() {
        UploadedFile file =
                new UploadedFile(
                        "movie.mp4", "video/mp4", 0, new ByteArrayInputStream(new byte[0]));

        assertThatThrownBy(() -> useCase.execute(UUID.randomUUID(), "owner@user.com", file))
                .isInstanceOf(InvalidUploadException.class);
    }

    @Test
    void rejectsNullFile() {
        assertThatThrownBy(() -> useCase.execute(UUID.randomUUID(), "owner@user.com", null))
                .isInstanceOf(InvalidUploadException.class);
    }
}
