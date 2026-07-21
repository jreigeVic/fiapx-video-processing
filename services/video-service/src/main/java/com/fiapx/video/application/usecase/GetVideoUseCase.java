package com.fiapx.video.application.usecase;

import com.fiapx.video.application.ports.out.VideoRepositoryPort;
import com.fiapx.video.domain.exception.VideoNotFoundException;
import com.fiapx.video.domain.model.Video;
import java.util.UUID;

public class GetVideoUseCase {

    private final VideoRepositoryPort videoRepositoryPort;

    public GetVideoUseCase(VideoRepositoryPort videoRepositoryPort) {
        this.videoRepositoryPort = videoRepositoryPort;
    }

    public Video execute(UUID ownerUserId, UUID videoId) {
        return videoRepositoryPort
                .findByIdAndOwnerUserId(videoId, ownerUserId)
                .orElseThrow(() -> new VideoNotFoundException(videoId));
    }
}
