package com.fiapx.processing.application.ports.out;

import com.fiapx.processing.domain.model.StorageObjectKey;
import java.nio.file.Path;

public interface StoragePort {

    Path downloadOriginal(StorageObjectKey key);

    void uploadResult(StorageObjectKey key, Path localZipFile);
}
