package com.fiapx.processing.infrastructure.adapter.out;

import com.fiapx.processing.application.ports.out.StoragePort;
import com.fiapx.processing.domain.exception.ProcessingFailedException;
import com.fiapx.processing.domain.model.StorageObjectKey;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

public class S3StorageAdapter implements StoragePort {

    private final S3Client s3Client;
    private final String bucket;

    public S3StorageAdapter(S3Client s3Client, String bucket) {
        this.s3Client = s3Client;
        this.bucket = bucket;
    }

    @Override
    public Path downloadOriginal(StorageObjectKey key) {
        try {
            // createTempFile reserves a unique name but also creates the (empty)
            // file - S3Client#getObject(request, Path) refuses to write to a
            // destination that already exists, so it must be removed first. The
            // SDK then recreates the file itself with default permissions, so
            // owner-only access (SonarCloud java:S5443 - the default temp
            // directory is world-writable on most systems) is re-applied
            // explicitly after the download completes rather than at
            // createTempFile time, which wouldn't survive the delete+recreate.
            Path target = Files.createTempFile("fiapx-source-", ".mp4");
            Files.delete(target);
            GetObjectRequest request =
                    GetObjectRequest.builder().bucket(bucket).key(key.value()).build();
            s3Client.getObject(request, target);
            restrictToOwnerOnly(target);
            return target;
        } catch (NoSuchKeyException e) {
            throw new ProcessingFailedException("SOURCE_FILE_NOT_FOUND", e);
        } catch (IOException e) {
            throw new ProcessingFailedException("PROCESSING_ERROR", e);
        }
    }

    @Override
    public void uploadResult(StorageObjectKey key, Path localZipFile) {
        PutObjectRequest request =
                PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(key.value())
                        .contentType("application/zip")
                        .build();
        s3Client.putObject(request, RequestBody.fromFile(localZipFile));
    }

    // No-op on non-POSIX filesystems (e.g. a developer running this outside
    // Docker on Windows), where this hardening doesn't apply the same way.
    private static void restrictToOwnerOnly(Path path) throws IOException {
        if (FileSystems.getDefault().supportedFileAttributeViews().contains("posix")) {
            Files.setPosixFilePermissions(path, PosixFilePermissions.fromString("rw-------"));
        }
    }
}
