package com.fiapx.video.infrastructure.adapter.out;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiapx.video.application.ports.out.EventPublisherPort;
import com.fiapx.video.domain.model.Video;
import java.time.Instant;
import java.util.UUID;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.CreateTopicRequest;
import software.amazon.awssdk.services.sns.model.PublishRequest;

public class SnsEventPublisherAdapter implements EventPublisherPort {

    private static final String PRODUCER = "Video Service";

    private final SnsClient snsClient;
    private final ObjectMapper objectMapper;
    private final String videoUploadedTopicName;
    private volatile String videoUploadedTopicArn;

    public SnsEventPublisherAdapter(
            SnsClient snsClient, ObjectMapper objectMapper, String videoUploadedTopicName) {
        this.snsClient = snsClient;
        this.objectMapper = objectMapper;
        this.videoUploadedTopicName = videoUploadedTopicName;
    }

    @Override
    public void publishVideoUploaded(Video video, String ownerEmail) {
        VideoUploadedPayload payload =
                new VideoUploadedPayload(
                        video.getId(),
                        video.getOwnerUserId(),
                        ownerEmail,
                        video.getOriginalFileName(),
                        video.getSourceObjectKey().value());

        EventEnvelope<VideoUploadedPayload> envelope =
                new EventEnvelope<>(
                        UUID.randomUUID(),
                        "VideoUploaded",
                        Instant.now(),
                        UUID.randomUUID(),
                        PRODUCER,
                        payload);

        publish(resolveTopicArn(), envelope);
    }

    private String resolveTopicArn() {
        if (videoUploadedTopicArn == null) {
            // create-topic is idempotent - safe even if the topic already
            // exists (e.g. provisioned by infrastructure/localstack/init-aws.sh).
            videoUploadedTopicArn =
                    snsClient
                            .createTopic(
                                    CreateTopicRequest.builder()
                                            .name(videoUploadedTopicName)
                                            .build())
                            .topicArn();
        }
        return videoUploadedTopicArn;
    }

    private void publish(String topicArn, Object envelope) {
        try {
            String message = objectMapper.writeValueAsString(envelope);
            snsClient.publish(PublishRequest.builder().topicArn(topicArn).message(message).build());
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize event envelope", e);
        }
    }
}
