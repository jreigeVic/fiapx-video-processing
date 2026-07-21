package com.fiapx.notification.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiapx.notification.application.ports.out.NotificationIdempotencyPort;
import com.fiapx.notification.application.ports.out.NotificationRepositoryPort;
import com.fiapx.notification.application.ports.out.NotificationSenderPort;
import com.fiapx.notification.application.usecase.NotifyVideoFailedUseCase;
import com.fiapx.notification.application.usecase.NotifyVideoProcessedUseCase;
import com.fiapx.notification.infrastructure.adapter.in.messaging.ProcessingNotificationConsumer;
import com.fiapx.notification.infrastructure.adapter.out.EmailNotificationAdapter;
import com.fiapx.notification.infrastructure.adapter.out.JpaNotificationIdempotencyAdapter;
import com.fiapx.notification.infrastructure.adapter.out.JpaNotificationRepositoryAdapter;
import com.fiapx.notification.infrastructure.repository.NotificationJpaRepository;
import com.fiapx.notification.infrastructure.repository.ProcessedEventJpaRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.sqs.SqsClient;

@Configuration
@EnableConfigurationProperties(AwsProperties.class)
@EnableScheduling
public class ApplicationConfiguration {

    @Bean
    public NotificationSenderPort notificationSenderPort(
            SesClient sesClient, AwsProperties awsProperties) {
        return new EmailNotificationAdapter(sesClient, awsProperties.getSes().getSender());
    }

    @Bean
    public NotificationRepositoryPort notificationRepositoryPort(
            NotificationJpaRepository notificationJpaRepository) {
        return new JpaNotificationRepositoryAdapter(notificationJpaRepository);
    }

    @Bean
    public NotificationIdempotencyPort notificationIdempotencyPort(
            ProcessedEventJpaRepository processedEventJpaRepository) {
        return new JpaNotificationIdempotencyAdapter(processedEventJpaRepository);
    }

    @Bean
    public NotifyVideoProcessedUseCase notifyVideoProcessedUseCase(
            NotificationSenderPort notificationSenderPort,
            NotificationRepositoryPort notificationRepositoryPort,
            NotificationIdempotencyPort notificationIdempotencyPort) {
        return new NotifyVideoProcessedUseCase(
                notificationSenderPort, notificationRepositoryPort, notificationIdempotencyPort);
    }

    @Bean
    public NotifyVideoFailedUseCase notifyVideoFailedUseCase(
            NotificationSenderPort notificationSenderPort,
            NotificationRepositoryPort notificationRepositoryPort,
            NotificationIdempotencyPort notificationIdempotencyPort) {
        return new NotifyVideoFailedUseCase(
                notificationSenderPort, notificationRepositoryPort, notificationIdempotencyPort);
    }

    @Bean
    @ConditionalOnProperty(
            prefix = "fiapx.aws.sqs",
            name = "consumer-enabled",
            havingValue = "true",
            matchIfMissing = true)
    public ProcessingNotificationConsumer processingNotificationConsumer(
            SqsClient sqsClient,
            ObjectMapper objectMapper,
            NotifyVideoProcessedUseCase notifyVideoProcessedUseCase,
            NotifyVideoFailedUseCase notifyVideoFailedUseCase,
            AwsProperties awsProperties) {
        return new ProcessingNotificationConsumer(
                sqsClient,
                objectMapper,
                notifyVideoProcessedUseCase,
                notifyVideoFailedUseCase,
                awsProperties.getSqs().getNotificationQueue());
    }
}
