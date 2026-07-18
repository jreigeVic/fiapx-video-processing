package com.fiapx.video.api.response;

import com.fiapx.video.domain.model.VideoStatus;
import java.util.UUID;

public record VideoUploadResponse(UUID videoId, VideoStatus status) {}
