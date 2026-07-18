package com.fiapx.video.infrastructure.adapter.out;

import com.fiapx.video.application.ports.out.VideoRepositoryPort;
import com.fiapx.video.domain.model.StorageObjectKey;
import com.fiapx.video.domain.model.Video;
import com.fiapx.video.domain.model.VideoStatus;
import com.fiapx.video.infrastructure.repository.VideoJpaEntity;
import com.fiapx.video.infrastructure.repository.VideoJpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class JpaVideoRepositoryAdapter implements VideoRepositoryPort {

    private final VideoJpaRepository jpaRepository;

    public JpaVideoRepositoryAdapter(VideoJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Video save(Video video) {
        VideoJpaEntity entity =
                new VideoJpaEntity(
                        video.getId(),
                        video.getOwnerUserId(),
                        video.getOriginalFileName(),
                        video.getSourceObjectKey().value(),
                        video.getResultObjectKey() == null
                                ? null
                                : video.getResultObjectKey().value(),
                        video.getStatus().name(),
                        video.getFailureReason(),
                        video.getCreatedAt(),
                        video.getUpdatedAt());
        jpaRepository.save(entity);
        return video;
    }

    @Override
    public Optional<Video> findByIdAndOwnerUserId(UUID id, UUID ownerUserId) {
        return jpaRepository.findByIdAndOwnerUserId(id, ownerUserId).map(this::toDomain);
    }

    @Override
    public Optional<Video> findById(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Video> findByOwnerUserId(UUID ownerUserId) {
        return jpaRepository.findByOwnerUserId(ownerUserId).stream().map(this::toDomain).toList();
    }

    private Video toDomain(VideoJpaEntity entity) {
        return Video.reconstruct(
                entity.getId(),
                entity.getOwnerUserId(),
                entity.getOriginalFileName(),
                StorageObjectKey.of(entity.getSourceObjectKey()),
                entity.getResultObjectKey() == null
                        ? null
                        : StorageObjectKey.of(entity.getResultObjectKey()),
                VideoStatus.valueOf(entity.getStatus()),
                entity.getFailureReason(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}
