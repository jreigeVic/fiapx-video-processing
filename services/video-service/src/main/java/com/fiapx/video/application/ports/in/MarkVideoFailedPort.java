package com.fiapx.video.application.ports.in;

import java.util.UUID;

public interface MarkVideoFailedPort {

    void execute(UUID eventId, UUID videoId, String failureReason);
}
