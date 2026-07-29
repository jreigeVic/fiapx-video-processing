package com.fiapx.processing.infrastructure.adapter.out;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import org.junit.jupiter.api.Test;

/**
 * createFramesDirectory() is package-private rather than exposed on the port - extractFrames() is
 * the only public entry point, and exercising it needs a real ffmpeg binary that's only available
 * inside the service's Docker image, not the Gradle test JVM.
 */
class FfmpegVideoProcessorAdapterTest {

    private final FfmpegVideoProcessorAdapter adapter =
            new FfmpegVideoProcessorAdapter("ffmpeg", 1);

    @Test
    void createsAnOwnerOnlyDirectoryOnPosixFilesystems() throws Exception {
        assumeTrue(FileSystems.getDefault().supportedFileAttributeViews().contains("posix"));

        Path directory = adapter.createFramesDirectory();
        try {
            assertThat(directory).exists().isDirectory();
            assertThat(Files.getPosixFilePermissions(directory))
                    .isEqualTo(PosixFilePermissions.fromString("rwx------"));
        } finally {
            Files.deleteIfExists(directory);
        }
    }

    @Test
    void createsADistinctDirectoryOnEachCall() throws Exception {
        Path first = adapter.createFramesDirectory();
        Path second = adapter.createFramesDirectory();
        try {
            assertThat(first).exists().isDirectory();
            assertThat(second).exists().isDirectory().isNotEqualTo(first);
        } finally {
            Files.deleteIfExists(first);
            Files.deleteIfExists(second);
        }
    }
}
