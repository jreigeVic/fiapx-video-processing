package com.fiapx.processing.configuration;

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

        public String getBucket() {
            return bucket;
        }

        public void setBucket(String bucket) {
            this.bucket = bucket;
        }
    }

    public static class Sns {
        private String videoProcessedTopic;
        private String videoFailedTopic;

        public String getVideoProcessedTopic() {
            return videoProcessedTopic;
        }

        public void setVideoProcessedTopic(String videoProcessedTopic) {
            this.videoProcessedTopic = videoProcessedTopic;
        }

        public String getVideoFailedTopic() {
            return videoFailedTopic;
        }

        public void setVideoFailedTopic(String videoFailedTopic) {
            this.videoFailedTopic = videoFailedTopic;
        }
    }

    public static class Sqs {
        private String videoProcessingQueue;

        public String getVideoProcessingQueue() {
            return videoProcessingQueue;
        }

        public void setVideoProcessingQueue(String videoProcessingQueue) {
            this.videoProcessingQueue = videoProcessingQueue;
        }
    }
}
