package hoang.com.auction_system_be.config.storage;

import java.net.URI;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
@EnableConfigurationProperties(ObjectStorageProperties.class)
public class ObjectStorageConfig {

    @Bean
    S3Client s3Client(ObjectStorageProperties properties) {
        var builder = S3Client.builder()
                .region(Region.of(properties.region()))
                .credentialsProvider(credentials(properties))
                .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(hasEndpoint(properties)).build());
        if (hasEndpoint(properties)) builder.endpointOverride(URI.create(properties.endpoint()));
        return builder.build();
    }

    @Bean
    S3Presigner s3Presigner(ObjectStorageProperties properties) {
        var builder = S3Presigner.builder()
                .region(Region.of(properties.region()))
                .credentialsProvider(credentials(properties))
                .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(hasEndpoint(properties)).build());
        if (hasEndpoint(properties)) builder.endpointOverride(URI.create(properties.endpoint()));
        return builder.build();
    }

    private AwsCredentialsProvider credentials(ObjectStorageProperties properties) {
        if (properties.useDefaultCredentials()) return DefaultCredentialsProvider.create();
        return StaticCredentialsProvider.create(
                AwsBasicCredentials.create(properties.accessKey(), properties.secretKey()));
    }

    private boolean hasEndpoint(ObjectStorageProperties properties) {
        return properties.endpoint() != null && !properties.endpoint().isBlank();
    }
}
