package com.fiapx.processing.infrastructure.adapter.out;

import com.fiapx.processing.application.dto.ProcessingOutput;
import com.fiapx.processing.application.ports.out.VideoProcessorPort;
import com.fiapx.processing.domain.exception.ProcessingFailedException;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/** Shells out to the system ffmpeg binary (installed in the service's Dockerfile). */
public class FfmpegVideoProcessorAdapter implements VideoProcessorPort {

    private static final long TIMEOUT_MINUTES = 10;
    private static final String PROCESSING_ERROR = "PROCESSING_ERROR";

    private final String ffmpegBinary;
    private final int frameRate;

    public FfmpegVideoProcessorAdapter(String ffmpegBinary, int frameRate) {
        this.ffmpegBinary = ffmpegBinary;
        this.frameRate = frameRate;
    }

    @Override
    public ProcessingOutput extractFrames(Path videoFile) {
        Path framesDir = createFramesDirectory();
        runFfmpeg(videoFile, framesDir);
        List<Path> frames = listFrames(framesDir);
        if (frames.isEmpty()) {
            throw new ProcessingFailedException("NO_FRAMES_EXTRACTED");
        }
        Path zipFile = zipFrames(framesDir, frames);
        return new ProcessingOutput(zipFile, frames.size());
    }

    private Path createFramesDirectory() {
        try {
            return Files.createTempDirectory("fiapx-frames-");
        } catch (IOException e) {
            throw new ProcessingFailedException(PROCESSING_ERROR, e);
        }
    }

    @SuppressWarnings("PMD.DoNotUseThreads")
    private void runFfmpeg(Path videoFile, Path framesDir) {
        List<String> command =
                List.of(
                        ffmpegBinary,
                        "-y",
                        "-i",
                        videoFile.toString(),
                        "-vf",
                        "fps=" + frameRate,
                        framesDir.resolve("frame_%04d.jpg").toString());
        try {
            Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
            boolean finished = process.waitFor(TIMEOUT_MINUTES, TimeUnit.MINUTES);
            if (!finished) {
                process.destroyForcibly();
                throw new ProcessingFailedException("PROCESSING_TIMEOUT");
            }
            if (process.exitValue() != 0) {
                throw new ProcessingFailedException(PROCESSING_ERROR);
            }
        } catch (IOException e) {
            throw new ProcessingFailedException(PROCESSING_ERROR, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ProcessingFailedException(PROCESSING_ERROR, e);
        }
    }

    private List<Path> listFrames(Path framesDir) {
        List<Path> frames = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(framesDir, "*.jpg")) {
            stream.forEach(frames::add);
        } catch (IOException e) {
            throw new ProcessingFailedException(PROCESSING_ERROR, e);
        }
        frames.sort(java.util.Comparator.comparing(Path::toString));
        return frames;
    }

    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private Path zipFrames(Path framesDir, List<Path> frames) {
        try {
            Path zipFile = framesDir.resolveSibling(framesDir.getFileName() + ".zip");
            try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(zipFile))) {
                for (Path frame : frames) {
                    zip.putNextEntry(new ZipEntry(frame.getFileName().toString()));
                    Files.copy(frame, zip);
                    zip.closeEntry();
                }
            }
            return zipFile;
        } catch (IOException e) {
            throw new ProcessingFailedException(PROCESSING_ERROR, e);
        }
    }
}
