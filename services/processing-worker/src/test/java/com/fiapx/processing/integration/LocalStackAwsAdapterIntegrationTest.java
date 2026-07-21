package com.fiapx.processing.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiapx.processing.application.ports.out.EventPublisherPort;
import com.fiapx.processing.application.ports.out.StoragePort;
import com.fiapx.processing.application.usecase.ProcessUploadedVideoUseCase;
import com.fiapx.processing.domain.model.FrameCount;
import com.fiapx.processing.domain.model.StorageObjectKey;
import com.fiapx.processing.infrastructure.adapter.in.messaging.VideoUploadedConsumer;
import com.fiapx.processing.infrastructure.adapter.out.S3StorageAdapter;
import com.fiapx.processing.infrastructure.adapter.out.SnsEventPublisherAdapter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
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
 * docs/LLD/processing-worker.md's testing strategy. Requires a running Docker daemon. Instantiates
 * adapters directly (no Spring context) - an adapter-level test, decoupled from the Postgres-backed
 * application context (see JpaIdempotencyAdapterIntegrationTest).
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
    static SnsClient snsClient;
    static SqsClient sqsClient;

    @BeforeAll
    static void setUpClients() {
        var endpoint = localstack.getEndpointOverride(LocalStackContainer.Service.S3);
        s3Client =
                S3Client.builder()
                        .endpointOverride(endpoint)
                        .region(Region.of(localstack.getRegion()))
                        .credentialsProvider(credentialsProvider())
                        .forcePathStyle(true)
                        .build();
        snsClient =
                SnsClient.builder()
                        .endpointOverride(endpoint)
                        .region(Region.of(localstack.getRegion()))
                        .credentialsProvider(credentialsProvider())
                        .build();
        sqsClient =
                SqsClient.builder()
                        .endpointOverride(endpoint)
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
    void downloadsOriginalAndUploadsResult() throws Exception {
        StorageObjectKey sourceKey =
                StorageObjectKey.of("videos/original/" + UUID.randomUUID() + ".mp4");
        byte[] sourceContent = "fake source video".getBytes(StandardCharsets.UTF_8);
        s3Client.putObject(
                PutObjectRequest.builder().bucket(BUCKET).key(sourceKey.value()).build(),
                RequestBody.fromBytes(sourceContent));

        StoragePort storagePort = new S3StorageAdapter(s3Client, BUCKET);
        Path downloaded = storagePort.downloadOriginal(sourceKey);
        assertThat(Files.readAllBytes(downloaded)).isEqualTo(sourceContent);

        StorageObjectKey resultKey =
                StorageObjectKey.of("videos/results/" + UUID.randomUUID() + ".zip");
        storagePort.uploadResult(resultKey, downloaded);

        byte[] roundTripped =
                s3Client.getObjectAsBytes(b -> b.bucket(BUCKET).key(resultKey.value()))
                        .asByteArray();
        assertThat(roundTripped).isEqualTo(sourceContent);
    }

    @Test
    void publishesVideoProcessedToSubscribedQueue() throws Exception {
        EventPublisherPort eventPublisherPort =
                new SnsEventPublisherAdapter(
                        snsClient, new ObjectMapper(), "video-processed", "video-failed");

        String topicArn =
                snsClient
                        .createTopic(CreateTopicRequest.builder().name("video-processed").build())
                        .topicArn();
        String queueUrl =
                sqsClient
                        .createQueue(
                                CreateQueueRequest.builder()
                                        .queueName("video-processed-test-queue")
                                        .build())
                        .queueUrl();
        snsClient.subscribe(
                SubscribeRequest.builder()
                        .topicArn(topicArn)
                        .protocol("sqs")
                        .endpoint(queueArn(queueUrl))
                        .attributes(Map.of("RawMessageDelivery", "true"))
                        .build());

        UUID videoId = UUID.randomUUID();
        eventPublisherPort.publishVideoProcessed(
                videoId,
                UUID.randomUUID(),
                "owner@test.com",
                StorageObjectKey.of("videos/results/x.zip"),
                FrameCount.of(10));

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
        assertThat(messages.get(0).body()).contains("VideoProcessed").contains(videoId.toString());
    }

    @Test
    void consumerProcessesSeededVideoUploadedMessage() throws Exception {
        String queueUrl =
                sqsClient
                        .createQueue(
                                CreateQueueRequest.builder()
                                        .queueName("video-uploaded-test-queue")
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
                        "VideoUploaded",
                        "payload",
                        Map.of(
                                "videoId", videoId,
                                "ownerUserId", ownerUserId,
                                "ownerEmail", "owner@test.com",
                                "originalFileName", "movie.mp4",
                                "sourceObjectKey", "videos/original/x.mp4"));
        sqsClient.sendMessage(
                SendMessageRequest.builder()
                        .queueUrl(queueUrl)
                        .messageBody(objectMapper.writeValueAsString(envelope))
                        .build());

        ProcessUploadedVideoUseCase processUploadedVideoUseCase =
                mock(ProcessUploadedVideoUseCase.class);
        VideoUploadedConsumer consumer =
                new VideoUploadedConsumer(
                        sqsClient,
                        objectMapper,
                        processUploadedVideoUseCase,
                        "video-uploaded-test-queue");

        consumer.poll();

        verify(processUploadedVideoUseCase)
                .execute(
                        eq(eventId),
                        eq(videoId),
                        eq(ownerUserId),
                        eq("owner@test.com"),
                        eq(StorageObjectKey.of("videos/original/x.mp4")));
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
