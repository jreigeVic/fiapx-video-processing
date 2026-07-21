package com.fiapx.processing.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiapx.processing.application.ports.out.EventPublisherPort;
import com.fiapx.processing.application.ports.out.IdempotencyPort;
import com.fiapx.processing.application.ports.out.StoragePort;
import com.fiapx.processing.application.ports.out.VideoProcessorPort;
import com.fiapx.processing.application.usecase.ProcessUploadedVideoUseCase;
import com.fiapx.processing.infrastructure.adapter.in.messaging.VideoUploadedConsumer;
import com.fiapx.processing.infrastructure.adapter.out.FfmpegVideoProcessorAdapter;
import com.fiapx.processing.infrastructure.adapter.out.JpaIdempotencyAdapter;
import com.fiapx.processing.infrastructure.adapter.out.S3StorageAdapter;
import com.fiapx.processing.infrastructure.adapter.out.SnsEventPublisherAdapter;
import com.fiapx.processing.infrastructure.repository.ProcessedEventJpaRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sqs.SqsClient;

@Configuration
@EnableConfigurationProperties(FfmpegProperties.class)
@EnableScheduling
public class ApplicationConfiguration {

    @Bean
    public StoragePort storagePort(S3Client s3Client, AwsProperties awsProperties) {
        return new S3StorageAdapter(s3Client, awsProperties.getS3().getBucket());
    }

    @Bean
    public VideoProcessorPort videoProcessorPort(FfmpegProperties ffmpegProperties) {
        return new FfmpegVideoProcessorAdapter(
                ffmpegProperties.getBinary(), ffmpegProperties.getFrameRate());
    }

    @Bean
    public EventPublisherPort eventPublisherPort(
            SnsClient snsClient, ObjectMapper objectMapper, AwsProperties awsProperties) {
        return new SnsEventPublisherAdapter(
                snsClient,
                objectMapper,
                awsProperties.getSns().getVideoProcessedTopic(),
                awsProperties.getSns().getVideoFailedTopic());
    }

    @Bean
    public IdempotencyPort idempotencyPort(
            ProcessedEventJpaRepository processedEventJpaRepository) {
        return new JpaIdempotencyAdapter(processedEventJpaRepository);
    }

    @Bean
    public ProcessUploadedVideoUseCase processUploadedVideoUseCase(
            StoragePort storagePort,
            VideoProcessorPort videoProcessorPort,
            EventPublisherPort eventPublisherPort,
            IdempotencyPort idempotencyPort) {
        return new ProcessUploadedVideoUseCase(
                storagePort, videoProcessorPort, eventPublisherPort, idempotencyPort);
    }

    @Bean
    @ConditionalOnProperty(
            prefix = "fiapx.aws.sqs",
            name = "consumer-enabled",
            havingValue = "true",
            matchIfMissing = true)
    public VideoUploadedConsumer videoUploadedConsumer(
            SqsClient sqsClient,
            ObjectMapper objectMapper,
            ProcessUploadedVideoUseCase processUploadedVideoUseCase,
            AwsProperties awsProperties) {
        return new VideoUploadedConsumer(
                sqsClient,
                objectMapper,
                processUploadedVideoUseCase,
                awsProperties.getSqs().getVideoProcessingQueue());
    }
}
