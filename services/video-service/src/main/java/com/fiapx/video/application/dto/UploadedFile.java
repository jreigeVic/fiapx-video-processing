package com.fiapx.video.application.dto;

import java.io.InputStream;

public record UploadedFile(
        String originalFileName, String contentType, long size, InputStream content) {}
