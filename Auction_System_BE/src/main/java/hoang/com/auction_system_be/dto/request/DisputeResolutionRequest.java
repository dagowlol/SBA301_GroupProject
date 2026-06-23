package hoang.com.auction_system_be.dto.request;

import hoang.com.auction_system_be.enums.DisputeStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DisputeResolutionRequest {

    @NotNull(message = "Status is required")
    DisputeStatus status;

    @NotBlank(message = "Resolution note is required")
    String resolution;
}
