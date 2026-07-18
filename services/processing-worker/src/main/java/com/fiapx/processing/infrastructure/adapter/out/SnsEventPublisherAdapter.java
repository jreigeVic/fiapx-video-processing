package com.fiapx.processing.infrastructure.adapter.out;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiapx.processing.application.ports.out.EventPublisherPort;
import com.fiapx.processing.domain.model.FailureReason;
import com.fiapx.processing.domain.model.FrameCount;
import com.fiapx.processing.domain.model.StorageObjectKey;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.CreateTopicRequest;
import software.amazon.awssdk.services.sns.model.PublishRequest;

public class SnsEventPublisherAdapter implements EventPublisherPort {

    private static final String PRODUCER = "Processing Worker";

    private final SnsClient snsClient;
    private final ObjectMapper objectMapper;
    private final String videoProcessedTopicName;
    private final String videoFailedTopicName;
    private final Map<String, String> resolvedTopicArns = new ConcurrentHashMap<>();

    public SnsEventPublisherAdapter(
            SnsClient snsClient,
            ObjectMapper objectMapper,
            String videoProcessedTopicName,
            String videoFailedTopicName) {
        this.snsClient = snsClient;
        this.objectMapper = objectMapper;
        this.videoProcessedTopicName = videoProcessedTopicName;
        this.videoFailedTopicName = videoFailedTopicName;
    }

    @Override
    public void publishVideoProcessed(
            UUID videoId,
            UUID ownerUserId,
            String ownerEmail,
            StorageObjectKey resultObjectKey,
            FrameCount frameCount) {
        VideoProcessedPayload payload =
                new VideoProcessedPayload(
                        videoId,
                        ownerUserId,
                        ownerEmail,
                        resultObjectKey.value(),
                        frameCount.value());
        EventEnvelope<VideoProcessedPayload> envelope =
                new EventEnvelope<>(
                        UUID.randomUUID(),
                        "VideoProcessed",
                        Instant.now(),
                        UUID.randomUUID(),
                        PRODUCER,
                        payload);
        publish(videoProcessedTopicName, envelope);
    }

    @Override
    public void publishVideoFailed(
            UUID videoId, UUID ownerUserId, String ownerEmail, FailureReason failureReason) {
        VideoFailedPayload payload =
                new VideoFailedPayload(videoId, ownerUserId, ownerEmail, failureReason.value());
        EventEnvelope<VideoFailedPayload> envelope =
                new EventEnvelope<>(
                        UUID.randomUUID(),
                        "VideoFailed",
                        Instant.now(),
                        UUID.randomUUID(),
                        PRODUCER,
                        payload);
        publish(videoFailedTopicName, envelope);
    }

    private String resolveTopicArn(String topicName) {
        // create-topic is idempotent - safe even if the topic already
        // exists (e.g. provisioned by infrastructure/localstack/init-aws.sh).
        return resolvedTopicArns.computeIfAbsent(
                topicName,
                name ->
                        snsClient
                                .createTopic(CreateTopicRequest.builder().name(name).build())
                                .topicArn());
    }

    private void publish(String topicName, Object envelope) {
        try {
            String message = objectMapper.writeValueAsString(envelope);
            snsClient.publish(
                    PublishRequest.builder()
                            .topicArn(resolveTopicArn(topicName))
                            .message(message)
                            .build());
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize event envelope", e);
        }
    }
}
