package com.fiapx.video.domain.exception;

import java.util.UUID;

public class VideoNotReadyForDownloadException extends RuntimeException {

    public VideoNotReadyForDownloadException(UUID videoId) {
        super("Video is not ready for download yet: " + videoId);
    }
}
