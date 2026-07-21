package com.fiapx.video.application.usecase;

import com.fiapx.video.application.ports.out.ProcessedEventIdempotencyPort;
import com.fiapx.video.application.ports.out.VideoRepositoryPort;
import com.fiapx.video.domain.exception.VideoNotFoundException;
import com.fiapx.video.domain.model.ProcessedEvent;
import com.fiapx.video.domain.model.Video;
import java.util.UUID;

public class MarkVideoFailedUseCase {

    private final VideoRepositoryPort videoRepositoryPort;
    private final ProcessedEventIdempotencyPort processedEventIdempotencyPort;

    public MarkVideoFailedUseCase(
            VideoRepositoryPort videoRepositoryPort,
            ProcessedEventIdempotencyPort processedEventIdempotencyPort) {
        this.videoRepositoryPort = videoRepositoryPort;
        this.processedEventIdempotencyPort = processedEventIdempotencyPort;
    }

    public void execute(UUID eventId, UUID videoId, String failureReason) {
        if (processedEventIdempotencyPort.existsByEventId(eventId)) {
            return;
        }

        Video video =
                videoRepositoryPort
                        .findById(videoId)
                        .orElseThrow(() -> new VideoNotFoundException(videoId));
        video.markFailed(failureReason);
        videoRepositoryPort.save(video);
        processedEventIdempotencyPort.save(ProcessedEvent.record(eventId, "VideoFailed"));
    }
}
