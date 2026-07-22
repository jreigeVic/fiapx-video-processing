package com.fiapx.processing.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fiapx.processing.application.dto.ProcessingOutput;
import com.fiapx.processing.application.ports.out.EventPublisherPort;
import com.fiapx.processing.application.ports.out.IdempotencyPort;
import com.fiapx.processing.application.ports.out.StoragePort;
import com.fiapx.processing.application.ports.out.VideoProcessorPort;
import com.fiapx.processing.application.usecase.ProcessUploadedVideoUseCase;
import com.fiapx.processing.domain.exception.ProcessingFailedException;
import com.fiapx.processing.domain.model.StorageObjectKey;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ProcessUploadedVideoUseCaseTest {

    private final StoragePort storagePort = mock(StoragePort.class);
    private final VideoProcessorPort videoProcessorPort = mock(VideoProcessorPort.class);
    private final EventPublisherPort eventPublisherPort = mock(EventPublisherPort.class);
    private final IdempotencyPort idempotencyPort = mock(IdempotencyPort.class);
    private final ProcessUploadedVideoUseCase useCase =
            new ProcessUploadedVideoUseCase(
                    storagePort, videoProcessorPort, eventPublisherPort, idempotencyPort);

    @Test
    void publishesVideoProcessedOnSuccess() {
        UUID videoId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        Path localFile = Path.of("input.mp4");
        Path zipFile = Path.of("output.zip");
        when(idempotencyPort.existsByEventId(eventId)).thenReturn(false);
        when(storagePort.downloadOriginal(any())).thenReturn(localFile);
        when(videoProcessorPort.extractFrames(localFile))
                .thenReturn(new ProcessingOutput(zipFile, 42));

        useCase.execute(
                eventId,
                videoId,
                ownerId,
                "owner@user.com",
                StorageObjectKey.of("videos/original/x.mp4"));

        verify(storagePort).uploadResult(any(), eq(zipFile));
        verify(eventPublisherPort)
                .publishVideoProcessed(
                        eq(videoId), eq(ownerId), eq("owner@user.com"), any(), any());
        verify(eventPublisherPort, never()).publishVideoFailed(any(), any(), any(), any());
        verify(idempotencyPort).save(any());
    }

    @Test
    void deletesTemporaryFilesAfterSuccessfulProcessing() throws IOException {
        Path localFile = Files.createTempFile("fiapx-test-source-", ".mp4");
        Path zipFile = Files.createTempFile("fiapx-test-result-", ".zip");
        UUID eventId = UUID.randomUUID();
        when(idempotencyPort.existsByEventId(eventId)).thenReturn(false);
        when(storagePort.downloadOriginal(any())).thenReturn(localFile);
        when(videoProcessorPort.extractFrames(localFile))
                .thenReturn(new ProcessingOutput(zipFile, 7));

        useCase.execute(
                eventId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                "owner@user.com",
                StorageObjectKey.of("videos/original/x.mp4"));

        assertThat(localFile).doesNotExist();
        assertThat(zipFile).doesNotExist();
    }

    @Test
    void deletesDownloadedOriginalEvenWhenProcessingFails() throws IOException {
        Path localFile = Files.createTempFile("fiapx-test-source-", ".mp4");
        UUID eventId = UUID.randomUUID();
        when(idempotencyPort.existsByEventId(eventId)).thenReturn(false);
        when(storagePort.downloadOriginal(any())).thenReturn(localFile);
        when(videoProcessorPort.extractFrames(localFile))
                .thenThrow(new ProcessingFailedException("PROCESSING_ERROR"));

        useCase.execute(
                eventId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                "owner@user.com",
                StorageObjectKey.of("videos/original/x.mp4"));

        assertThat(localFile).doesNotExist();
    }

    @Test
    void publishesVideoFailedOnKnownProcessingFailure() {
        UUID videoId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        when(idempotencyPort.existsByEventId(eventId)).thenReturn(false);
        when(storagePort.downloadOriginal(any()))
                .thenThrow(new ProcessingFailedException("SOURCE_FILE_NOT_FOUND"));

        useCase.execute(
                eventId,
                videoId,
                ownerId,
                "owner@user.com",
                StorageObjectKey.of("videos/original/x.mp4"));

        verify(eventPublisherPort)
                .publishVideoFailed(eq(videoId), eq(ownerId), eq("owner@user.com"), any());
        verify(eventPublisherPort, never())
                .publishVideoProcessed(any(), any(), any(), any(), any());
        verify(idempotencyPort).save(any());
    }

    @Test
    void publishesVideoFailedOnUnexpectedException() {
        UUID videoId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        when(idempotencyPort.existsByEventId(eventId)).thenReturn(false);
        when(storagePort.downloadOriginal(any())).thenThrow(new RuntimeException("boom"));

        useCase.execute(
                eventId,
                videoId,
                ownerId,
                "owner@user.com",
                StorageObjectKey.of("videos/original/x.mp4"));

        verify(eventPublisherPort)
                .publishVideoFailed(eq(videoId), eq(ownerId), eq("owner@user.com"), any());
    }

    @Test
    void ignoresDuplicateEvent() {
        UUID eventId = UUID.randomUUID();
        when(idempotencyPort.existsByEventId(eventId)).thenReturn(true);

        useCase.execute(
                eventId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                "owner@user.com",
                StorageObjectKey.of("videos/original/x.mp4"));

        verify(storagePort, never()).downloadOriginal(any());
    }
}
