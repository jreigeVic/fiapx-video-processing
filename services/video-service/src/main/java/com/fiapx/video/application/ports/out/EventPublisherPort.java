package com.fiapx.video.application.ports.out;

import com.fiapx.video.domain.model.Video;

public interface EventPublisherPort {

    void publishVideoUploaded(Video video, String ownerEmail);
}
