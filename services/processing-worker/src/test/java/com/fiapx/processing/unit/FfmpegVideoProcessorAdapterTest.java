package com.fiapx.processing.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.fiapx.processing.infrastructure.adapter.out.FfmpegVideoProcessorAdapter;
import java.lang.reflect.Method;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import org.junit.jupiter.api.Test;

/**
 * createFramesDirectory() is private (extractFrames() is the only port-facing entry point, and
 * exercising it needs a real ffmpeg binary that isn't available to the Gradle test JVM - only
 * inside the service's Docker image), so it's invoked via reflection here rather than widening
 * its visibility just for this test.
 */
class FfmpegVideoProcessorAdapterTest {

    private final FfmpegVideoProcessorAdapter adapter =
            new FfmpegVideoProcessorAdapter("ffmpeg", 1);

    @Test
    void createsAnOwnerOnlyDirectoryOnPosixFilesystems() throws Exception {
        assumeTrue(FileSystems.getDefault().supportedFileAttributeViews().contains("posix"));

        Path directory = invokeCreateFramesDirectory();
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
        Path first = invokeCreateFramesDirectory();
        Path second = invokeCreateFramesDirectory();
        try {
            assertThat(first).exists().isDirectory();
            assertThat(second).exists().isDirectory().isNotEqualTo(first);
        } finally {
            Files.deleteIfExists(first);
            Files.deleteIfExists(second);
        }
    }

    private Path invokeCreateFramesDirectory() throws Exception {
        Method method =
                FfmpegVideoProcessorAdapter.class.getDeclaredMethod("createFramesDirectory");
        method.setAccessible(true);
        return (Path) method.invoke(adapter);
    }
}
