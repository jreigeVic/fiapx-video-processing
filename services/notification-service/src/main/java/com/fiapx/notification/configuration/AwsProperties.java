package com.fiapx.notification.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "fiapx.aws")
public class AwsProperties {

    private String region;
    private String endpointOverride;
    private final Ses ses = new Ses();
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

    public Ses getSes() {
        return ses;
    }

    public Sqs getSqs() {
        return sqs;
    }

    public static class Ses {
        private String sender;

        public String getSender() {
            return sender;
        }

        public void setSender(String sender) {
            this.sender = sender;
        }
    }

    public static class Sqs {
        private String notificationQueue;

        public String getNotificationQueue() {
            return notificationQueue;
        }

        public void setNotificationQueue(String notificationQueue) {
            this.notificationQueue = notificationQueue;
        }
    }
}
