package hoang.com.auction_system_be.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DisputeRequest {

    @NotNull(message = "Session ID is required")
    Long sessionId;

    @NotBlank(message = "Dispute type is required")
    String type;

    @NotBlank(message = "Dispute description is required")
    String description;
}
