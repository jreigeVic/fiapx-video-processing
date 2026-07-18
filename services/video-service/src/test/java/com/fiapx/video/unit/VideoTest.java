package com.fiapx.video.unit;

import static org.assertj.core.api.Assertions.assertThat;

import com.fiapx.video.domain.model.StorageObjectKey;
import com.fiapx.video.domain.model.Video;
import com.fiapx.video.domain.model.VideoStatus;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class VideoTest {

    @Test
    void receivedVideoStartsAsReceived() {
        Video video =
                Video.receive(
                        UUID.randomUUID(),
                        "movie.mp4",
                        StorageObjectKey.of("videos/original/x.mp4"));
        assertThat(video.getStatus()).isEqualTo(VideoStatus.RECEIVED);
        assertThat(video.isDownloadable()).isFalse();
    }

    @Test
    void markProcessingTransitionsStatus() {
        Video video =
                Video.receive(
                        UUID.randomUUID(),
                        "movie.mp4",
                        StorageObjectKey.of("videos/original/x.mp4"));
        video.markProcessing();
        assertThat(video.getStatus()).isEqualTo(VideoStatus.PROCESSING);
    }

    @Test
    void markProcessedSetsResultKeyAndClearsFailureReason() {
        Video video =
                Video.receive(
                        UUID.randomUUID(),
                        "movie.mp4",
                        StorageObjectKey.of("videos/original/x.mp4"));
        video.markFailed("temporary error");
        video.markProcessed(StorageObjectKey.of("videos/results/x.zip"));

        assertThat(video.getStatus()).isEqualTo(VideoStatus.PROCESSED);
        assertThat(video.getResultObjectKey())
                .isEqualTo(StorageObjectKey.of("videos/results/x.zip"));
        assertThat(video.getFailureReason()).isNull();
        assertThat(video.isDownloadable()).isTrue();
    }

    @Test
    void markFailedSetsReason() {
        Video video =
                Video.receive(
                        UUID.randomUUID(),
                        "movie.mp4",
                        StorageObjectKey.of("videos/original/x.mp4"));
        video.markFailed("PROCESSING_ERROR");

        assertThat(video.getStatus()).isEqualTo(VideoStatus.FAILED);
        assertThat(video.getFailureReason()).isEqualTo("PROCESSING_ERROR");
        assertThat(video.isDownloadable()).isFalse();
    }

    @Test
    void isOwnedByChecksOwnerUserId() {
        UUID ownerId = UUID.randomUUID();
        Video video =
                Video.receive(ownerId, "movie.mp4", StorageObjectKey.of("videos/original/x.mp4"));

        assertThat(video.isOwnedBy(ownerId)).isTrue();
        assertThat(video.isOwnedBy(UUID.randomUUID())).isFalse();
    }
}
