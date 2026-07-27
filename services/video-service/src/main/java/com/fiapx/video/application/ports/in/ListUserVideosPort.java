package com.fiapx.video.application.ports.in;

import com.fiapx.video.domain.model.Video;
import com.fiapx.video.domain.model.VideoStatus;
import java.util.List;
import java.util.UUID;

public interface ListUserVideosPort {

    List<Video> execute(UUID ownerUserId, VideoStatus statusFilter);
}
