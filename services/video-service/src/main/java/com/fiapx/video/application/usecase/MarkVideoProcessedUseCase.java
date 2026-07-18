package com.fiapx.video.application.usecase;

import com.fiapx.video.application.ports.out.ProcessedEventIdempotencyPort;
import com.fiapx.video.application.ports.out.VideoRepositoryPort;
import com.fiapx.video.domain.exception.VideoNotFoundException;
import com.fiapx.video.domain.model.ProcessedEvent;
import com.fiapx.video.domain.model.StorageObjectKey;
import com.fiapx.video.domain.model.Video;
import java.util.UUID;

public class MarkVideoProcessedUseCase {

    private final VideoRepositoryPort videoRepositoryPort;
    private final ProcessedEventIdempotencyPort processedEventIdempotencyPort;

    public MarkVideoProcessedUseCase(
            VideoRepositoryPort videoRepositoryPort,
            ProcessedEventIdempotencyPort processedEventIdempotencyPort) {
        this.videoRepositoryPort = videoRepositoryPort;
        this.processedEventIdempotencyPort = processedEventIdempotencyPort;
    }

    public void execute(UUID eventId, UUID videoId, StorageObjectKey resultObjectKey) {
        if (processedEventIdempotencyPort.existsByEventId(eventId)) {
            return;
        }

        Video video =
                videoRepositoryPort
                        .findById(videoId)
                        .orElseThrow(() -> new VideoNotFoundException(videoId));
        video.markProcessed(resultObjectKey);
        videoRepositoryPort.save(video);
        processedEventIdempotencyPort.save(ProcessedEvent.record(eventId, "VideoProcessed"));
    }
}
