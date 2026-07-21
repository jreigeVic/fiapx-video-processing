package com.fiapx.video.configuration;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "fiapx.upload")
public class UploadProperties {

    private long maxFileSizeBytes = 104_857_600; // 100MB
    private List<String> allowedContentTypes =
            List.of(
                    "video/mp4",
                    "video/mpeg",
                    "video/quicktime",
                    "video/x-msvideo",
                    "video/x-matroska");

    public long getMaxFileSizeBytes() {
        return maxFileSizeBytes;
    }

    public void setMaxFileSizeBytes(long maxFileSizeBytes) {
        this.maxFileSizeBytes = maxFileSizeBytes;
    }

    public List<String> getAllowedContentTypes() {
        return allowedContentTypes;
    }

    public void setAllowedContentTypes(List<String> allowedContentTypes) {
        this.allowedContentTypes = allowedContentTypes;
    }
}
