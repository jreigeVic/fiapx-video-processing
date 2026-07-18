package com.fiapx.video.api.mapper;

import com.fiapx.video.api.response.DownloadUrlResponse;
import com.fiapx.video.api.response.VideoResponse;
import com.fiapx.video.api.response.VideoUploadResponse;
import com.fiapx.video.application.dto.DownloadUrl;
import com.fiapx.video.domain.model.Video;
import java.util.UUID;

public final class VideoMapper {

    private VideoMapper() {}

    public static VideoUploadResponse toUploadResponse(Video video) {
        return new VideoUploadResponse(video.getId(), video.getStatus());
    }

    public static VideoResponse toResponse(Video video) {
        return new VideoResponse(
                video.getId(),
                video.getOriginalFileName(),
                video.getStatus(),
                video.getCreatedAt(),
                video.getUpdatedAt(),
                video.isDownloadable());
    }

    public static DownloadUrlResponse toDownloadUrlResponse(UUID videoId, DownloadUrl downloadUrl) {
        return new DownloadUrlResponse(videoId, downloadUrl.url(), downloadUrl.expiresAt());
    }
}
