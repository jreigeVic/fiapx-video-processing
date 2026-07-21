package com.fiapx.processing.unit;

import static org.assertj.core.api.Assertions.assertThat;

import com.fiapx.processing.domain.model.ProcessingJob;
import com.fiapx.processing.domain.model.ProcessingStatus;
import com.fiapx.processing.domain.model.StorageObjectKey;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ProcessingJobTest {

    @Test
    void startsAsProcessing() {
        ProcessingJob job =
                ProcessingJob.start(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        StorageObjectKey.of("videos/original/x.mp4"));
        assertThat(job.getStatus()).isEqualTo(ProcessingStatus.PROCESSING);
    }

    @Test
    void succeedSetsResultKeyAndStatus() {
        ProcessingJob job =
                ProcessingJob.start(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        StorageObjectKey.of("videos/original/x.mp4"));
        StorageObjectKey resultKey = StorageObjectKey.of("videos/results/x.zip");

        job.succeed(resultKey);

        assertThat(job.getStatus()).isEqualTo(ProcessingStatus.SUCCEEDED);
        assertThat(job.getResultObjectKey()).isEqualTo(resultKey);
    }

    @Test
    void failSetsStatus() {
        ProcessingJob job =
                ProcessingJob.start(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        StorageObjectKey.of("videos/original/x.mp4"));

        job.fail();

        assertThat(job.getStatus()).isEqualTo(ProcessingStatus.FAILED);
    }
}
