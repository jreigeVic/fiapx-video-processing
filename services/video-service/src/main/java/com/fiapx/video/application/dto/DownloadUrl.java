package com.fiapx.video.application.dto;

import java.time.Instant;

public record DownloadUrl(String url, Instant expiresAt) {}
