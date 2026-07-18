package com.fiapx.notification.infrastructure.adapter.out;

import com.fiapx.notification.application.ports.out.NotificationSenderPort;
import com.fiapx.notification.domain.exception.NotificationDeliveryException;
import java.util.UUID;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.Body;
import software.amazon.awssdk.services.ses.model.Content;
import software.amazon.awssdk.services.ses.model.Destination;
import software.amazon.awssdk.services.ses.model.Message;
import software.amazon.awssdk.services.ses.model.MessageRejectedException;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;

/** Sends notifications via AWS SES (LocalStack in dev/test, real SES in production). */
public class EmailNotificationAdapter implements NotificationSenderPort {

    private final SesClient sesClient;
    private final String senderAddress;

    public EmailNotificationAdapter(SesClient sesClient, String senderAddress) {
        this.sesClient = sesClient;
        this.senderAddress = senderAddress;
    }

    @Override
    public void sendVideoProcessed(
            String recipientEmail, UUID videoId, String resultObjectKey, int frameCount) {
        send(
                recipientEmail,
                "Your video has finished processing",
                "Video "
                        + videoId
                        + " was processed successfully into "
                        + frameCount
                        + " frames. Result: "
                        + resultObjectKey);
    }

    @Override
    public void sendVideoFailed(String recipientEmail, UUID videoId, String failureReason) {
        send(
                recipientEmail,
                "Your video processing failed",
                "Video " + videoId + " failed to process: " + failureReason);
    }

    private void send(String recipientEmail, String subject, String body) {
        try {
            sesClient.sendEmail(
                    SendEmailRequest.builder()
                            .source(senderAddress)
                            .destination(Destination.builder().toAddresses(recipientEmail).build())
                            .message(
                                    Message.builder()
                                            .subject(Content.builder().data(subject).build())
                                            .body(
                                                    Body.builder()
                                                            .text(
                                                                    Content.builder()
                                                                            .data(body)
                                                                            .build())
                                                            .build())
                                            .build())
                            .build());
        } catch (MessageRejectedException e) {
            throw new NotificationDeliveryException("RECIPIENT_REJECTED", e);
        }
    }
}
