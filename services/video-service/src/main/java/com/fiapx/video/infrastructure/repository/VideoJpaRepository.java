package com.fiapx.video.infrastructure.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VideoJpaRepository extends JpaRepository<VideoJpaEntity, UUID> {

    Optional<VideoJpaEntity> findByIdAndOwnerUserId(UUID id, UUID ownerUserId);

    List<VideoJpaEntity> findByOwnerUserId(UUID ownerUserId);
}
