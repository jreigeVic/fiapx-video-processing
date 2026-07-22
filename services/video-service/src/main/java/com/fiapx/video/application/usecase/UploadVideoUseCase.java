package com.fiapx.video.application.usecase;

import com.fiapx.video.application.dto.UploadedFile;
import com.fiapx.video.application.ports.out.EventPublisherPort;
import com.fiapx.video.application.ports.out.StoragePort;
import com.fiapx.video.application.ports.out.VideoRepositoryPort;
import com.fiapx.video.domain.exception.InvalidUploadException;
import com.fiapx.video.domain.model.StorageObjectKey;
import com.fiapx.video.domain.model.Video;
import java.util.Set;
import java.util.UUID;

public class UploadVideoUseCase {

    private final VideoRepositoryPort videoRepositoryPort;
    private final StoragePort storagePort;
    private final EventPublisherPort eventPublisherPort;
    private final long maxFileSizeBytes;
    private final Set<String> allowedContentTypes;

    public UploadVideoUseCase(
            VideoRepositoryPort videoRepositoryPort,
            StoragePort storagePort,
            EventPublisherPort eventPublisherPort,
            long maxFileSizeBytes,
            Set<String> allowedContentTypes) {
        this.videoRepositoryPort = videoRepositoryPort;
        this.storagePort = storagePort;
        this.eventPublisherPort = eventPublisherPort;
        this.maxFileSizeBytes = maxFileSizeBytes;
        this.allowedContentTypes = allowedContentTypes;
    }

    public Video execute(UUID ownerUserId, String ownerEmail, UploadedFile file) {
        validate(file);

        StorageObjectKey sourceKey =
                StorageObjectKey.of(
                        "videos/original/" + UUID.randomUUID() + "-" + file.originalFileName());
        storagePort.store(sourceKey, file.content(), file.size(), file.contentType());

        Video video = Video.receive(ownerUserId, file.originalFileName(), sourceKey);
        videoRepositoryPort.save(video);

        eventPublisherPort.publishVideoUploaded(video, ownerEmail);
        video.markProcessing();
        return videoRepositoryPort.save(video);
    }

    private void validate(UploadedFile file) {
        validateNotEmpty(file);
        validateMaxSize(file);
        validateContentType(file);
    }

    private void validateNotEmpty(UploadedFile file) {
        if (file == null
                || file.size() <= 0
                || file.originalFileName() == null
                || file.originalFileName().isBlank()) {
            throw new InvalidUploadException("Uploaded file must not be empty");
        }
    }

    private void validateMaxSize(UploadedFile file) {
        if (file.size() > maxFileSizeBytes) {
            throw new InvalidUploadException(
                    "Uploaded file exceeds the maximum allowed size of "
                            + maxFileSizeBytes
                            + " bytes");
        }
    }

    private void validateContentType(UploadedFile file) {
        if (!allowedContentTypes.contains(file.contentType())) {
            throw new InvalidUploadException("Unsupported content type: " + file.contentType());
        }
    }
}
