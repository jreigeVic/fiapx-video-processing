package com.fiapx.video.application.ports.out;

import com.fiapx.video.domain.model.StorageObjectKey;
import java.io.InputStream;
import java.time.Duration;

public interface StoragePort {

    void store(StorageObjectKey key, InputStream content, long size, String contentType);

    String generatePresignedDownloadUrl(StorageObjectKey key, Duration timeToLive);
}
