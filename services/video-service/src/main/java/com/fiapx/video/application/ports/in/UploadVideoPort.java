package com.fiapx.video.application.ports.in;

import com.fiapx.video.application.dto.UploadedFile;
import com.fiapx.video.domain.model.Video;
import java.util.UUID;

public interface UploadVideoPort {

    Video execute(UUID ownerUserId, String ownerEmail, UploadedFile file);
}
