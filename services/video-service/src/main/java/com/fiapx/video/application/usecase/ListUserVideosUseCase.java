package com.fiapx.video.application.usecase;

import com.fiapx.video.application.ports.out.VideoRepositoryPort;
import com.fiapx.video.domain.model.Video;
import com.fiapx.video.domain.model.VideoStatus;
import java.util.List;
import java.util.UUID;

public class ListUserVideosUseCase {

    private final VideoRepositoryPort videoRepositoryPort;

    public ListUserVideosUseCase(VideoRepositoryPort videoRepositoryPort) {
        this.videoRepositoryPort = videoRepositoryPort;
    }

    public List<Video> execute(UUID ownerUserId, VideoStatus statusFilter) {
        List<Video> videos = videoRepositoryPort.findByOwnerUserId(ownerUserId);
        if (statusFilter == null) {
            return videos;
        }
        return videos.stream().filter(video -> video.getStatus() == statusFilter).toList();
    }
}
