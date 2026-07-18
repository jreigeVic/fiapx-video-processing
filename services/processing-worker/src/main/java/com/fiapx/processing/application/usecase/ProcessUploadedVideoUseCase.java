package com.fiapx.processing.application.usecase;

import com.fiapx.processing.application.dto.ProcessingOutput;
import com.fiapx.processing.application.ports.out.EventPublisherPort;
import com.fiapx.processing.application.ports.out.IdempotencyPort;
import com.fiapx.processing.application.ports.out.StoragePort;
import com.fiapx.processing.application.ports.out.VideoProcessorPort;
import com.fiapx.processing.domain.exception.ProcessingFailedException;
import com.fiapx.processing.domain.model.FailureReason;
import com.fiapx.processing.domain.model.FrameCount;
import com.fiapx.processing.domain.model.ProcessedEvent;
import com.fiapx.processing.domain.model.ProcessingJob;
import com.fiapx.processing.domain.model.StorageObjectKey;
import java.nio.file.Path;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessUploadedVideoUseCase {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessUploadedVideoUseCase.class);

    private final StoragePort storagePort;
    private final VideoProcessorPort videoProcessorPort;
    private final EventPublisherPort eventPublisherPort;
    private final IdempotencyPort idempotencyPort;

    public ProcessUploadedVideoUseCase(
            StoragePort storagePort,
            VideoProcessorPort videoProcessorPort,
            EventPublisherPort eventPublisherPort,
            IdempotencyPort idempotencyPort) {
        this.storagePort = storagePort;
        this.videoProcessorPort = videoProcessorPort;
        this.eventPublisherPort = eventPublisherPort;
        this.idempotencyPort = idempotencyPort;
    }

    public void execute(
            UUID eventId,
            UUID videoId,
            UUID ownerUserId,
            String ownerEmail,
            StorageObjectKey sourceObjectKey) {
        if (idempotencyPort.existsByEventId(eventId)) {
            return;
        }

        ProcessingJob job = ProcessingJob.start(videoId, ownerUserId, sourceObjectKey);

        try {
            Path originalFile = storagePort.downloadOriginal(sourceObjectKey);
            ProcessingOutput output = videoProcessorPort.extractFrames(originalFile);

            StorageObjectKey resultKey = StorageObjectKey.of("videos/results/" + videoId + ".zip");
            storagePort.uploadResult(resultKey, output.zipFile());
            job.succeed(resultKey);

            eventPublisherPort.publishVideoProcessed(
                    videoId,
                    ownerUserId,
                    ownerEmail,
                    resultKey,
                    FrameCount.of(output.frameCount()));
        } catch (ProcessingFailedException e) {
            job.fail();
            eventPublisherPort.publishVideoFailed(
                    videoId, ownerUserId, ownerEmail, FailureReason.of(e.getMessage()));
        } catch (RuntimeException e) {
            LOGGER.error("Unexpected failure processing video {}", videoId, e);
            job.fail();
            eventPublisherPort.publishVideoFailed(
                    videoId, ownerUserId, ownerEmail, FailureReason.of("PROCESSING_ERROR"));
        }

        idempotencyPort.save(ProcessedEvent.record(eventId, "VideoUploaded"));
    }
}
