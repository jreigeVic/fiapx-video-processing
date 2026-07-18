package com.fiapx.processing.application.ports.out;

import com.fiapx.processing.application.dto.ProcessingOutput;
import java.nio.file.Path;

public interface VideoProcessorPort {

    ProcessingOutput extractFrames(Path videoFile);
}
