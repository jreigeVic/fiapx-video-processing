package com.fiapx.video.infrastructure.adapter.out;

import com.fiapx.video.application.ports.out.StoragePort;
import com.fiapx.video.domain.model.StorageObjectKey;
import java.io.InputStream;
import java.time.Duration;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

public class S3StorageAdapter implements StoragePort {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final String bucket;

    public S3StorageAdapter(S3Client s3Client, S3Presigner s3Presigner, String bucket) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
        this.bucket = bucket;
    }

    @Override
    public void store(StorageObjectKey key, InputStream content, long size, String contentType) {
        PutObjectRequest request =
                PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(key.value())
                        .contentType(contentType)
                        .build();
        s3Client.putObject(request, RequestBody.fromInputStream(content, size));
    }

    @Override
    public String generatePresignedDownloadUrl(StorageObjectKey key, Duration timeToLive) {
        GetObjectRequest getObjectRequest =
                GetObjectRequest.builder().bucket(bucket).key(key.value()).build();
        GetObjectPresignRequest presignRequest =
                GetObjectPresignRequest.builder()
                        .signatureDuration(timeToLive)
                        .getObjectRequest(getObjectRequest)
                        .build();
        return s3Presigner.presignGetObject(presignRequest).url().toString();
    }
}
