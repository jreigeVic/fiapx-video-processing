package com.fiapx.video.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiapx.video.application.ports.out.EventPublisherPort;
import com.fiapx.video.application.ports.out.ProcessedEventIdempotencyPort;
import com.fiapx.video.application.ports.out.StoragePort;
import com.fiapx.video.application.ports.out.TokenValidatorPort;
import com.fiapx.video.application.ports.out.VideoRepositoryPort;
import com.fiapx.video.application.usecase.GenerateDownloadUrlUseCase;
import com.fiapx.video.application.usecase.GetVideoUseCase;
import com.fiapx.video.application.usecase.ListUserVideosUseCase;
import com.fiapx.video.application.usecase.MarkVideoFailedUseCase;
import com.fiapx.video.application.usecase.MarkVideoProcessedUseCase;
import com.fiapx.video.application.usecase.UploadVideoUseCase;
import com.fiapx.video.infrastructure.adapter.in.JwtAuthenticationFilter;
import com.fiapx.video.infrastructure.adapter.in.RestAuthenticationEntryPoint;
import com.fiapx.video.infrastructure.adapter.out.JpaProcessedEventIdempotencyAdapter;
import com.fiapx.video.infrastructure.adapter.out.JpaVideoRepositoryAdapter;
import com.fiapx.video.infrastructure.adapter.out.JwtTokenValidatorAdapter;
import com.fiapx.video.infrastructure.adapter.out.S3StorageAdapter;
import com.fiapx.video.infrastructure.adapter.out.SnsEventPublisherAdapter;
import com.fiapx.video.infrastructure.messaging.ProcessingResultConsumer;
import com.fiapx.video.infrastructure.repository.ProcessedEventJpaRepository;
import com.fiapx.video.infrastructure.repository.VideoJpaRepository;
import java.time.Duration;
import java.util.HashSet;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sqs.SqsClient;

@Configuration
@EnableConfigurationProperties({JwtProperties.class, UploadProperties.class})
@EnableScheduling
public class ApplicationConfiguration {

    @Bean
    public TokenValidatorPort tokenValidatorPort(JwtProperties jwtProperties) {
        return new JwtTokenValidatorAdapter(jwtProperties.getSecret());
    }

    @Bean
    public VideoRepositoryPort videoRepositoryPort(VideoJpaRepository videoJpaRepository) {
        return new JpaVideoRepositoryAdapter(videoJpaRepository);
    }

    @Bean
    public ProcessedEventIdempotencyPort processedEventIdempotencyPort(
            ProcessedEventJpaRepository processedEventJpaRepository) {
        return new JpaProcessedEventIdempotencyAdapter(processedEventJpaRepository);
    }

    @Bean
    public StoragePort storagePort(
            S3Client s3Client, S3Presigner s3Presigner, AwsProperties awsProperties) {
        return new S3StorageAdapter(s3Client, s3Presigner, awsProperties.getS3().getBucket());
    }

    @Bean
    public EventPublisherPort eventPublisherPort(
            SnsClient snsClient, ObjectMapper objectMapper, AwsProperties awsProperties) {
        return new SnsEventPublisherAdapter(
                snsClient, objectMapper, awsProperties.getSns().getVideoUploadedTopic());
    }

    @Bean
    public UploadVideoUseCase uploadVideoUseCase(
            VideoRepositoryPort videoRepositoryPort,
            StoragePort storagePort,
            EventPublisherPort eventPublisherPort,
            UploadProperties uploadProperties) {
        return new UploadVideoUseCase(
                videoRepositoryPort,
                storagePort,
                eventPublisherPort,
                uploadProperties.getMaxFileSizeBytes(),
                new HashSet<>(uploadProperties.getAllowedContentTypes()));
    }

    @Bean
    public GetVideoUseCase getVideoUseCase(VideoRepositoryPort videoRepositoryPort) {
        return new GetVideoUseCase(videoRepositoryPort);
    }

    @Bean
    public ListUserVideosUseCase listUserVideosUseCase(VideoRepositoryPort videoRepositoryPort) {
        return new ListUserVideosUseCase(videoRepositoryPort);
    }

    @Bean
    public GenerateDownloadUrlUseCase generateDownloadUrlUseCase(
            VideoRepositoryPort videoRepositoryPort,
            StoragePort storagePort,
            AwsProperties awsProperties) {
        return new GenerateDownloadUrlUseCase(
                videoRepositoryPort,
                storagePort,
                Duration.ofSeconds(awsProperties.getS3().getDownloadUrlTtlSeconds()));
    }

    @Bean
    public MarkVideoProcessedUseCase markVideoProcessedUseCase(
            VideoRepositoryPort videoRepositoryPort,
            ProcessedEventIdempotencyPort processedEventIdempotencyPort) {
        return new MarkVideoProcessedUseCase(videoRepositoryPort, processedEventIdempotencyPort);
    }

    @Bean
    public MarkVideoFailedUseCase markVideoFailedUseCase(
            VideoRepositoryPort videoRepositoryPort,
            ProcessedEventIdempotencyPort processedEventIdempotencyPort) {
        return new MarkVideoFailedUseCase(videoRepositoryPort, processedEventIdempotencyPort);
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(TokenValidatorPort tokenValidatorPort) {
        return new JwtAuthenticationFilter(tokenValidatorPort);
    }

    @Bean
    public RestAuthenticationEntryPoint restAuthenticationEntryPoint() {
        return new RestAuthenticationEntryPoint();
    }

    @Bean
    @ConditionalOnProperty(
            prefix = "fiapx.aws.sqs",
            name = "consumer-enabled",
            havingValue = "true",
            matchIfMissing = true)
    public ProcessingResultConsumer processingResultConsumer(
            SqsClient sqsClient,
            ObjectMapper objectMapper,
            MarkVideoProcessedUseCase markVideoProcessedUseCase,
            MarkVideoFailedUseCase markVideoFailedUseCase,
            AwsProperties awsProperties) {
        return new ProcessingResultConsumer(
                sqsClient,
                objectMapper,
                markVideoProcessedUseCase,
                markVideoFailedUseCase,
                awsProperties.getSqs().getVideoResultsQueue());
    }
}
