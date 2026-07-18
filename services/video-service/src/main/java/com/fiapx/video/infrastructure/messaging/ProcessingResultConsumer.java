package com.fiapx.video.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiapx.video.application.usecase.MarkVideoFailedUseCase;
import com.fiapx.video.application.usecase.MarkVideoProcessedUseCase;
import com.fiapx.video.domain.model.StorageObjectKey;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

/** Polls video-results-queue for VideoProcessed/VideoFailed and updates video_db. */
public class ProcessingResultConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessingResultConsumer.class);
    private static final int MAX_MESSAGES_PER_POLL = 10;
    private static final int WAIT_TIME_SECONDS = 2;

    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;
    private final MarkVideoProcessedUseCase markVideoProcessedUseCase;
    private final MarkVideoFailedUseCase markVideoFailedUseCase;
    private final String queueName;

    public ProcessingResultConsumer(
            SqsClient sqsClient,
            ObjectMapper objectMapper,
            MarkVideoProcessedUseCase markVideoProcessedUseCase,
            MarkVideoFailedUseCase markVideoFailedUseCase,
            String queueName) {
        this.sqsClient = sqsClient;
        this.objectMapper = objectMapper;
        this.markVideoProcessedUseCase = markVideoProcessedUseCase;
        this.markVideoFailedUseCase = markVideoFailedUseCase;
        this.queueName = queueName;
    }

    @Scheduled(fixedDelayString = "${fiapx.aws.sqs.poll-delay-ms:5000}")
    public void poll() {
        String queueUrl = sqsClient.getQueueUrl(builder -> builder.queueName(queueName)).queueUrl();
        List<Message> messages =
                sqsClient
                        .receiveMessage(
                                ReceiveMessageRequest.builder()
                                        .queueUrl(queueUrl)
                                        .maxNumberOfMessages(MAX_MESSAGES_PER_POLL)
                                        .waitTimeSeconds(WAIT_TIME_SECONDS)
                                        .build())
                        .messages();

        for (Message message : messages) {
            if (handle(message)) {
                sqsClient.deleteMessage(
                        DeleteMessageRequest.builder()
                                .queueUrl(queueUrl)
                                .receiptHandle(message.receiptHandle())
                                .build());
            }
        }
    }

    private boolean handle(Message message) {
        try {
            InboundEventEnvelope envelope =
                    objectMapper.readValue(message.body(), InboundEventEnvelope.class);
            switch (envelope.eventType()) {
                case "VideoProcessed" -> {
                    VideoProcessedPayload payload =
                            objectMapper.treeToValue(
                                    envelope.payload(), VideoProcessedPayload.class);
                    markVideoProcessedUseCase.execute(
                            envelope.eventId(),
                            payload.videoId(),
                            StorageObjectKey.of(payload.resultObjectKey()));
                }
                case "VideoFailed" -> {
                    VideoFailedPayload payload =
                            objectMapper.treeToValue(envelope.payload(), VideoFailedPayload.class);
                    markVideoFailedUseCase.execute(
                            envelope.eventId(), payload.videoId(), payload.failureReason());
                }
                default -> LOGGER.warn("Ignoring unknown event type: {}", envelope.eventType());
            }
            return true;
        } catch (Exception e) {
            LOGGER.error(
                    "Failed to process message, leaving for SQS redrive: {}",
                    message.messageId(),
                    e);
            return false;
        }
    }
}
