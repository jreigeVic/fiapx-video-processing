package com.fiapx.video.application.ports.in;

import com.fiapx.video.application.dto.DownloadUrl;
import java.util.UUID;

public interface GenerateDownloadUrlPort {

    DownloadUrl execute(UUID ownerUserId, UUID videoId);
}
