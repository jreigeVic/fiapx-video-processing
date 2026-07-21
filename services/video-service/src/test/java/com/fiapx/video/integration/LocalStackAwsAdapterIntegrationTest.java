package com.fiapx.video.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fiapx.video.application.ports.out.EventPublisherPort;
import com.fiapx.video.application.ports.out.StoragePort;
import com.fiapx.video.application.usecase.MarkVideoFailedUseCase;
import com.fiapx.video.application.usecase.MarkVideoProcessedUseCase;
import com.fiapx.video.domain.model.StorageObjectKey;
import com.fiapx.video.domain.model.Video;
import com.fiapx.video.infrastructure.adapter.out.S3StorageAdapter;
import com.fiapx.video.infrastructure.adapter.out.SnsEventPublisherAdapter;
import com.fiapx.video.infrastructure.messaging.ProcessingResultConsumer;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
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
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.CreateTopicRequest;
import software.amazon.awssdk.services.sns.model.SubscribeRequest;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueAttributesRequest;
import software.amazon.awssdk.services.sqs.model.QueueAttributeName;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

/**
 * Exercises the S3/SNS/SQS adapters against a real (LocalStack-emulated) AWS API, per
 * docs/LLD/video-service.md's testing strategy. Requires a running Docker daemon. Instantiates
 * adapters directly (no Spring context) - this is an adapter-level test, not an application-level
 * one; the H2/Postgres-backed application context is exercised separately (see
 * VideoControllerIntegrationTest and JpaVideoRepositoryAdapterIntegrationTest).
 */
@Testcontainers
class LocalStackAwsAdapterIntegrationTest {

    private static final String BUCKET = "fiapx-videos-test";

    @Container
    static LocalStackContainer localstack =
            new LocalStackContainer(DockerImageName.parse("localstack/localstack:3"))
                    .withServices(
                            LocalStackContainer.Service.S3,
                            LocalStackContainer.Service.SNS,
                            LocalStackContainer.Service.SQS);

    static S3Client s3Client;
    static S3Presigner s3Presigner;
    static SnsClient snsClient;
    static SqsClient sqsClient;

    @BeforeAll
    static void setUpClients() {
        s3Client =
                S3Client.builder()
                        .endpointOverride(
                                localstack.getEndpointOverride(LocalStackContainer.Service.S3))
                        .region(Region.of(localstack.getRegion()))
                        .credentialsProvider(credentialsProvider())
                        .forcePathStyle(true)
                        .build();
        s3Presigner =
                S3Presigner.builder()
                        .endpointOverride(
                                localstack.getEndpointOverride(LocalStackContainer.Service.S3))
                        .region(Region.of(localstack.getRegion()))
                        .credentialsProvider(credentialsProvider())
                        .build();
        snsClient =
                SnsClient.builder()
                        .endpointOverride(
                                localstack.getEndpointOverride(LocalStackContainer.Service.S3))
                        .region(Region.of(localstack.getRegion()))
                        .credentialsProvider(credentialsProvider())
                        .build();
        sqsClient =
                SqsClient.builder()
                        .endpointOverride(
                                localstack.getEndpointOverride(LocalStackContainer.Service.S3))
                        .region(Region.of(localstack.getRegion()))
                        .credentialsProvider(credentialsProvider())
                        .build();

        s3Client.createBucket(CreateBucketRequest.builder().bucket(BUCKET).build());
    }

    private static StaticCredentialsProvider credentialsProvider() {
        return StaticCredentialsProvider.create(
                AwsBasicCredentials.create(localstack.getAccessKey(), localstack.getSecretKey()));
    }

    @Test
    void storesAndDownloadsViaPresignedUrl() throws Exception {
        StoragePort storagePort = new S3StorageAdapter(s3Client, s3Presigner, BUCKET);
        StorageObjectKey key = StorageObjectKey.of("videos/original/" + UUID.randomUUID() + ".mp4");
        byte[] content = "fake video bytes".getBytes(StandardCharsets.UTF_8);

        storagePort.store(key, new ByteArrayInputStream(content), content.length, "video/mp4");
        String presignedUrl = storagePort.generatePresignedDownloadUrl(key, Duration.ofMinutes(5));

        HttpResponse<byte[]> response =
                HttpClient.newHttpClient()
                        .send(
                                HttpRequest.newBuilder(URI.create(presignedUrl)).GET().build(),
                                HttpResponse.BodyHandlers.ofByteArray());

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).isEqualTo(content);
    }

    @Test
    void publishesVideoUploadedToSubscribedQueue() throws Exception {
        // SnsEventPublisherAdapter's EventEnvelope has an Instant field - the
        // production ObjectMapper bean gets JavaTimeModule from Spring Boot's
        // Jackson auto-configuration, so a plain `new ObjectMapper()` here
        // needs the same module registered explicitly to serialize it.
        EventPublisherPort eventPublisherPort =
                new SnsEventPublisherAdapter(
                        snsClient,
                        new ObjectMapper().registerModule(new JavaTimeModule()),
                        "video-uploaded");

        String topicArn =
                snsClient
                        .createTopic(CreateTopicRequest.builder().name("video-uploaded").build())
                        .topicArn();
        String queueUrl =
                sqsClient
                        .createQueue(
                                CreateQueueRequest.builder()
                                        .queueName("video-uploaded-test-queue")
                                        .build())
                        .queueUrl();
        String queueArn = queueArn(queueUrl);
        snsClient.subscribe(
                SubscribeRequest.builder()
                        .topicArn(topicArn)
                        .protocol("sqs")
                        .endpoint(queueArn)
                        .attributes(Map.of("RawMessageDelivery", "true"))
                        .build());

        UUID ownerUserId = UUID.randomUUID();
        Video video =
                Video.receive(
                        ownerUserId, "movie.mp4", StorageObjectKey.of("videos/original/x.mp4"));
        eventPublisherPort.publishVideoUploaded(video, "owner@test.com");

        var messages =
                sqsClient
                        .receiveMessage(
                                ReceiveMessageRequest.builder()
                                        .queueUrl(queueUrl)
                                        .waitTimeSeconds(10)
                                        .maxNumberOfMessages(1)
                                        .build())
                        .messages();

        assertThat(messages).hasSize(1);
        assertThat(messages.get(0).body())
                .contains("VideoUploaded")
                .contains(video.getId().toString());
    }

    @Test
    void consumerProcessesSeededVideoProcessedMessage() throws Exception {
        String queueUrl =
                sqsClient
                        .createQueue(
                                CreateQueueRequest.builder()
                                        .queueName("video-results-test-queue")
                                        .build())
                        .queueUrl();
        ObjectMapper objectMapper = new ObjectMapper();
        UUID eventId = UUID.randomUUID();
        UUID videoId = UUID.randomUUID();
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
                                UUID.randomUUID(),
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

        MarkVideoProcessedUseCase markVideoProcessedUseCase = mock(MarkVideoProcessedUseCase.class);
        MarkVideoFailedUseCase markVideoFailedUseCase = mock(MarkVideoFailedUseCase.class);
        ProcessingResultConsumer consumer =
                new ProcessingResultConsumer(
                        sqsClient,
                        objectMapper,
                        markVideoProcessedUseCase,
                        markVideoFailedUseCase,
                        "video-results-test-queue");

        consumer.poll();

        verify(markVideoProcessedUseCase)
                .execute(eq(eventId), eq(videoId), eq(StorageObjectKey.of("videos/results/x.zip")));
    }

    private String queueArn(String queueUrl) {
        return sqsClient
                .getQueueAttributes(
                        GetQueueAttributesRequest.builder()
                                .queueUrl(queueUrl)
                                .attributeNames(QueueAttributeName.QUEUE_ARN)
                                .build())
                .attributes()
                .get(QueueAttributeName.QUEUE_ARN);
    }
}
