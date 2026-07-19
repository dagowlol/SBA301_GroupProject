package hoang.com.auction_system_be.dto.response;

import java.time.Instant;

public record PresignedUploadResponse(String objectKey, String uploadUrl, Instant expiresAt) {
}
