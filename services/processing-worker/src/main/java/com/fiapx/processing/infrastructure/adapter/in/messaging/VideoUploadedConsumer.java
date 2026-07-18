package com.fiapx.processing.infrastructure.adapter.in.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiapx.processing.application.usecase.ProcessUploadedVideoUseCase;
import com.fiapx.processing.domain.model.StorageObjectKey;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

/** Polls video-processing-queue for VideoUploaded and triggers frame extraction. */
public class VideoUploadedConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(VideoUploadedConsumer.class);
    private static final int MAX_MESSAGES_PER_POLL = 10;
    private static final int WAIT_TIME_SECONDS = 2;

    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;
    private final ProcessUploadedVideoUseCase processUploadedVideoUseCase;
    private final String queueName;

    public VideoUploadedConsumer(
            SqsClient sqsClient,
            ObjectMapper objectMapper,
            ProcessUploadedVideoUseCase processUploadedVideoUseCase,
            String queueName) {
        this.sqsClient = sqsClient;
        this.objectMapper = objectMapper;
        this.processUploadedVideoUseCase = processUploadedVideoUseCase;
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
            if (!"VideoUploaded".equals(envelope.eventType())) {
                LOGGER.warn("Ignoring unknown event type: {}", envelope.eventType());
                return true;
            }
            VideoUploadedPayload payload =
                    objectMapper.treeToValue(envelope.payload(), VideoUploadedPayload.class);
            processUploadedVideoUseCase.execute(
                    envelope.eventId(),
                    payload.videoId(),
                    payload.ownerUserId(),
                    payload.ownerEmail(),
                    StorageObjectKey.of(payload.sourceObjectKey()));
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
