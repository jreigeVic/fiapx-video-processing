package com.fiapx.video.application.ports.in;

import com.fiapx.video.domain.model.Video;
import java.util.UUID;

public interface GetVideoPort {

    Video execute(UUID ownerUserId, UUID videoId);
}
