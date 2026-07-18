package com.fiapx.notification.infrastructure.adapter.in.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiapx.notification.application.usecase.NotifyVideoFailedUseCase;
import com.fiapx.notification.application.usecase.NotifyVideoProcessedUseCase;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

/** Polls notification-queue for VideoProcessed and VideoFailed and dispatches notifications. */
public class ProcessingNotificationConsumer {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(ProcessingNotificationConsumer.class);
    private static final int MAX_MESSAGES_PER_POLL = 10;
    private static final int WAIT_TIME_SECONDS = 2;

    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;
    private final NotifyVideoProcessedUseCase notifyVideoProcessedUseCase;
    private final NotifyVideoFailedUseCase notifyVideoFailedUseCase;
    private final String queueName;

    public ProcessingNotificationConsumer(
            SqsClient sqsClient,
            ObjectMapper objectMapper,
            NotifyVideoProcessedUseCase notifyVideoProcessedUseCase,
            NotifyVideoFailedUseCase notifyVideoFailedUseCase,
            String queueName) {
        this.sqsClient = sqsClient;
        this.objectMapper = objectMapper;
        this.notifyVideoProcessedUseCase = notifyVideoProcessedUseCase;
        this.notifyVideoFailedUseCase = notifyVideoFailedUseCase;
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
                case "VideoProcessed" -> handleVideoProcessed(envelope);
                case "VideoFailed" -> handleVideoFailed(envelope);
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

    private void handleVideoProcessed(InboundEventEnvelope envelope) throws Exception {
        VideoProcessedPayload payload =
                objectMapper.treeToValue(envelope.payload(), VideoProcessedPayload.class);
        notifyVideoProcessedUseCase.execute(
                envelope.eventId(),
                payload.videoId(),
                payload.ownerUserId(),
                payload.ownerEmail(),
                payload.resultObjectKey(),
                payload.frameCount());
    }

    private void handleVideoFailed(InboundEventEnvelope envelope) throws Exception {
        VideoFailedPayload payload =
                objectMapper.treeToValue(envelope.payload(), VideoFailedPayload.class);
        notifyVideoFailedUseCase.execute(
                envelope.eventId(),
                payload.videoId(),
                payload.ownerUserId(),
                payload.ownerEmail(),
                payload.failureReason());
    }
}
