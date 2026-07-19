package hoang.com.auction_system_be.config.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.storage")
public record ObjectStorageProperties(
        String bucket,
        String region,
        String endpoint,
        String accessKey,
        String secretKey,
        boolean useDefaultCredentials,
        String publicBaseUrl,
        int uploadUrlExpiryMinutes,
        int downloadUrlExpiryMinutes,
        long maxImageSizeBytes) {
}
