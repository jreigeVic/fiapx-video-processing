package com.fiapx.notification.integration;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiapx.notification.application.usecase.NotifyVideoFailedUseCase;
import com.fiapx.notification.application.usecase.NotifyVideoProcessedUseCase;
import com.fiapx.notification.infrastructure.adapter.in.messaging.ProcessingNotificationConsumer;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

/**
 * Exercises {@link ProcessingNotificationConsumer} against a real (LocalStack-emulated) SQS API,
 * per docs/LLD/notification-service.md's testing strategy (no S3/SNS use in this service - it only
 * consumes SQS and calls SES, the latter covered by unit tests with a mocked port). Requires a
 * running Docker daemon.
 */
@Testcontainers
class LocalStackSqsConsumerIntegrationTest {

    @Container
    static LocalStackContainer localstack =
            new LocalStackContainer(DockerImageName.parse("localstack/localstack:3"))
                    .withServices(LocalStackContainer.Service.SQS);

    static SqsClient sqsClient;

    @BeforeAll
    static void setUpClients() {
        sqsClient =
                SqsClient.builder()
                        .endpointOverride(
                                localstack.getEndpointOverride(LocalStackContainer.Service.SQS))
                        .region(Region.of(localstack.getRegion()))
                        .credentialsProvider(
                                StaticCredentialsProvider.create(
                                        AwsBasicCredentials.create(
                                                localstack.getAccessKey(),
                                                localstack.getSecretKey())))
                        .build();
    }

    @Test
    void consumerProcessesSeededVideoProcessedMessage() throws Exception {
        String queueUrl =
                sqsClient
                        .createQueue(
                                CreateQueueRequest.builder()
                                        .queueName("notification-test-queue")
                                        .build())
                        .queueUrl();
        ObjectMapper objectMapper = new ObjectMapper();
        UUID eventId = UUID.randomUUID();
        UUID videoId = UUID.randomUUID();
        UUID ownerUserId = UUID.randomUUID();
        Map<String, Object> envelope =
                Map.of(
                        "eventId",
                        eventId,
                        "eventType",
                        "VideoProcessed",
                        "payload",
                        Map.of(
                                "videoId",
                                videoId,
                                "ownerUserId",
                                ownerUserId,
                                "ownerEmail",
                                "owner@test.com",
                                "resultObjectKey",
                                "videos/results/x.zip",
                                "frameCount",
                                42));
        sqsClient.sendMessage(
                SendMessageRequest.builder()
                        .queueUrl(queueUrl)
                        .messageBody(objectMapper.writeValueAsString(envelope))
                        .build());

        NotifyVideoProcessedUseCase notifyVideoProcessedUseCase =
                mock(NotifyVideoProcessedUseCase.class);
        NotifyVideoFailedUseCase notifyVideoFailedUseCase = mock(NotifyVideoFailedUseCase.class);
        ProcessingNotificationConsumer consumer =
                new ProcessingNotificationConsumer(
                        sqsClient,
                        objectMapper,
                        notifyVideoProcessedUseCase,
                        notifyVideoFailedUseCase,
                        "notification-test-queue");

        consumer.poll();

        verify(notifyVideoProcessedUseCase)
                .execute(
                        eq(eventId),
                        eq(videoId),
                        eq(ownerUserId),
                        eq("owner@test.com"),
                        eq("videos/results/x.zip"),
                        eq(42));
    }

    @Test
    void consumerProcessesSeededVideoFailedMessage() throws Exception {
        String queueUrl =
                sqsClient
                        .createQueue(
                                CreateQueueRequest.builder()
                                        .queueName("notification-failed-test-queue")
                                        .build())
                        .queueUrl();
        ObjectMapper objectMapper = new ObjectMapper();
        UUID eventId = UUID.randomUUID();
        UUID videoId = UUID.randomUUID();
        UUID ownerUserId = UUID.randomUUID();
        Map<String, Object> envelope =
                Map.of(
                        "eventId",
                        eventId,
                        "eventType",
                        "VideoFailed",
                        "payload",
                        Map.of(
                                "videoId",
                                videoId,
                                "ownerUserId",
                                ownerUserId,
                                "ownerEmail",
                                "owner@test.com",
                                "failureReason",
                                "PROCESSING_ERROR"));
        sqsClient.sendMessage(
                SendMessageRequest.builder()
                        .queueUrl(queueUrl)
                        .messageBody(objectMapper.writeValueAsString(envelope))
                        .build());

        NotifyVideoProcessedUseCase notifyVideoProcessedUseCase =
                mock(NotifyVideoProcessedUseCase.class);
        NotifyVideoFailedUseCase notifyVideoFailedUseCase = mock(NotifyVideoFailedUseCase.class);
        ProcessingNotificationConsumer consumer =
                new ProcessingNotificationConsumer(
                        sqsClient,
                        objectMapper,
                        notifyVideoProcessedUseCase,
                        notifyVideoFailedUseCase,
                        "notification-failed-test-queue");

        consumer.poll();

        verify(notifyVideoFailedUseCase)
                .execute(
                        eq(eventId),
                        eq(videoId),
                        eq(ownerUserId),
                        eq("owner@test.com"),
                        eq("PROCESSING_ERROR"));
    }
}
