package com.fiapx.video.configuration;

import java.net.URI;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sqs.SqsClient;

/**
 * When fiapx.aws.endpoint-override is set (LocalStack, per docker-compose.yml), clients point there
 * with static test credentials. Left blank, clients use the default AWS endpoint/credential chain.
 */
@Configuration
@EnableConfigurationProperties(AwsProperties.class)
public class AwsClientConfiguration {

    @Bean
    public AwsCredentialsProvider awsCredentialsProvider(AwsProperties awsProperties) {
        if (hasEndpointOverride(awsProperties)) {
            return StaticCredentialsProvider.create(AwsBasicCredentials.create("test", "test"));
        }
        return DefaultCredentialsProvider.create();
    }

    @Bean
    public S3Client s3Client(
            AwsProperties awsProperties, AwsCredentialsProvider credentialsProvider) {
        var builder =
                S3Client.builder()
                        .region(Region.of(awsProperties.getRegion()))
                        .credentialsProvider(credentialsProvider)
                        .forcePathStyle(hasEndpointOverride(awsProperties));
        if (hasEndpointOverride(awsProperties)) {
            builder.endpointOverride(URI.create(awsProperties.getEndpointOverride()));
        }
        return builder.build();
    }

    @Bean
    public S3Presigner s3Presigner(
            AwsProperties awsProperties, AwsCredentialsProvider credentialsProvider) {
        var builder =
                S3Presigner.builder()
                        .region(Region.of(awsProperties.getRegion()))
                        .credentialsProvider(credentialsProvider);
        if (hasEndpointOverride(awsProperties)) {
            builder.endpointOverride(URI.create(awsProperties.getEndpointOverride()));
        }
        return builder.build();
    }

    @Bean
    public SnsClient snsClient(
            AwsProperties awsProperties, AwsCredentialsProvider credentialsProvider) {
        var builder =
                SnsClient.builder()
                        .region(Region.of(awsProperties.getRegion()))
                        .credentialsProvider(credentialsProvider);
        if (hasEndpointOverride(awsProperties)) {
            builder.endpointOverride(URI.create(awsProperties.getEndpointOverride()));
        }
        return builder.build();
    }

    @Bean
    public SqsClient sqsClient(
            AwsProperties awsProperties, AwsCredentialsProvider credentialsProvider) {
        var builder =
                SqsClient.builder()
                        .region(Region.of(awsProperties.getRegion()))
                        .credentialsProvider(credentialsProvider);
        if (hasEndpointOverride(awsProperties)) {
            builder.endpointOverride(URI.create(awsProperties.getEndpointOverride()));
        }
        return builder.build();
    }

    private boolean hasEndpointOverride(AwsProperties awsProperties) {
        return awsProperties.getEndpointOverride() != null
                && !awsProperties.getEndpointOverride().isBlank();
    }
}
