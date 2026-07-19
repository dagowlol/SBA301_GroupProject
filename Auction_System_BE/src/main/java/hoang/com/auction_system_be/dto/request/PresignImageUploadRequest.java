package hoang.com.auction_system_be.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record PresignImageUploadRequest(
        @NotBlank String fileName,
        @NotBlank String contentType,
        @Min(1) @Max(5_242_880) long fileSize) {
}
