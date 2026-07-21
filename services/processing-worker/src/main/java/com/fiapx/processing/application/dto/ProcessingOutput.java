package com.fiapx.processing.application.dto;

import java.nio.file.Path;

public record ProcessingOutput(Path zipFile, int frameCount) {}
