package com.fiapx.video.application.ports.out;

import com.fiapx.video.domain.model.Video;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VideoRepositoryPort {

    Video save(Video video);

    Optional<Video> findByIdAndOwnerUserId(UUID id, UUID ownerUserId);

    Optional<Video> findById(UUID id);

    List<Video> findByOwnerUserId(UUID ownerUserId);
}
