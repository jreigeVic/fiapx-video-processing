package com.fiapx.video.api.response;

import java.time.Instant;
import java.util.UUID;

public record DownloadUrlResponse(UUID videoId, String url, Instant expiresAt) {}
