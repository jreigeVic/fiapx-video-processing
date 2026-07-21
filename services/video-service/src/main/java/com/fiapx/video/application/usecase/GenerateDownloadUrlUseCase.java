package com.fiapx.video.application.usecase;

import com.fiapx.video.application.dto.DownloadUrl;
import com.fiapx.video.application.ports.out.StoragePort;
import com.fiapx.video.application.ports.out.VideoRepositoryPort;
import com.fiapx.video.domain.exception.VideoNotFoundException;
import com.fiapx.video.domain.exception.VideoNotReadyForDownloadException;
import com.fiapx.video.domain.model.Video;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

public class GenerateDownloadUrlUseCase {

    private final VideoRepositoryPort videoRepositoryPort;
    private final StoragePort storagePort;
    private final Duration urlTimeToLive;

    public GenerateDownloadUrlUseCase(
            VideoRepositoryPort videoRepositoryPort,
            StoragePort storagePort,
            Duration urlTimeToLive) {
        this.videoRepositoryPort = videoRepositoryPort;
        this.storagePort = storagePort;
        this.urlTimeToLive = urlTimeToLive;
    }

    public DownloadUrl execute(UUID ownerUserId, UUID videoId) {
        Video video =
                videoRepositoryPort
                        .findByIdAndOwnerUserId(videoId, ownerUserId)
                        .orElseThrow(() -> new VideoNotFoundException(videoId));

        if (!video.isDownloadable()) {
            throw new VideoNotReadyForDownloadException(videoId);
        }

        String url =
                storagePort.generatePresignedDownloadUrl(video.getResultObjectKey(), urlTimeToLive);
        return new DownloadUrl(url, Instant.now().plus(urlTimeToLive));
    }
}
