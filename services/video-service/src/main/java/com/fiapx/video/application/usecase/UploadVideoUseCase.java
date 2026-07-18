package com.fiapx.video.application.usecase;

import com.fiapx.video.application.dto.UploadedFile;
import com.fiapx.video.application.ports.out.EventPublisherPort;
import com.fiapx.video.application.ports.out.StoragePort;
import com.fiapx.video.application.ports.out.VideoRepositoryPort;
import com.fiapx.video.domain.exception.InvalidUploadException;
import com.fiapx.video.domain.model.StorageObjectKey;
import com.fiapx.video.domain.model.Video;
import java.util.UUID;

public class UploadVideoUseCase {

    private final VideoRepositoryPort videoRepositoryPort;
    private final StoragePort storagePort;
    private final EventPublisherPort eventPublisherPort;

    public UploadVideoUseCase(
            VideoRepositoryPort videoRepositoryPort,
            StoragePort storagePort,
            EventPublisherPort eventPublisherPort) {
        this.videoRepositoryPort = videoRepositoryPort;
        this.storagePort = storagePort;
        this.eventPublisherPort = eventPublisherPort;
    }

    public Video execute(UUID ownerUserId, String ownerEmail, UploadedFile file) {
        if (file == null
                || file.size() <= 0
                || file.originalFileName() == null
                || file.originalFileName().isBlank()) {
            throw new InvalidUploadException("Uploaded file must not be empty");
        }

        StorageObjectKey sourceKey =
                StorageObjectKey.of(
                        "videos/original/" + UUID.randomUUID() + "-" + file.originalFileName());
        Video video = Video.receive(ownerUserId, file.originalFileName(), sourceKey);
        videoRepositoryPort.save(video);

        storagePort.store(sourceKey, file.content(), file.size(), file.contentType());

        eventPublisherPort.publishVideoUploaded(video, ownerEmail);
        video.markProcessing();
        return videoRepositoryPort.save(video);
    }
}
