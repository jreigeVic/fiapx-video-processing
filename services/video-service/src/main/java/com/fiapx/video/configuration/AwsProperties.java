package com.fiapx.video.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "fiapx.aws")
public class AwsProperties {

    private String region;
    private String endpointOverride;
    private final S3 s3 = new S3();
    private final Sns sns = new Sns();
    private final Sqs sqs = new Sqs();

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getEndpointOverride() {
        return endpointOverride;
    }

    public void setEndpointOverride(String endpointOverride) {
        this.endpointOverride = endpointOverride;
    }

    public S3 getS3() {
        return s3;
    }

    public Sns getSns() {
        return sns;
    }

    public Sqs getSqs() {
        return sqs;
    }

    public static class S3 {
        private String bucket;
        private long downloadUrlTtlSeconds = 900;

        public String getBucket() {
            return bucket;
        }

        public void setBucket(String bucket) {
            this.bucket = bucket;
        }

        public long getDownloadUrlTtlSeconds() {
            return downloadUrlTtlSeconds;
        }

        public void setDownloadUrlTtlSeconds(long downloadUrlTtlSeconds) {
            this.downloadUrlTtlSeconds = downloadUrlTtlSeconds;
        }
    }

    public static class Sns {
        private String videoUploadedTopic;

        public String getVideoUploadedTopic() {
            return videoUploadedTopic;
        }

        public void setVideoUploadedTopic(String videoUploadedTopic) {
            this.videoUploadedTopic = videoUploadedTopic;
        }
    }

    public static class Sqs {
        private String videoResultsQueue;

        public String getVideoResultsQueue() {
            return videoResultsQueue;
        }

        public void setVideoResultsQueue(String videoResultsQueue) {
            this.videoResultsQueue = videoResultsQueue;
        }
    }
}
