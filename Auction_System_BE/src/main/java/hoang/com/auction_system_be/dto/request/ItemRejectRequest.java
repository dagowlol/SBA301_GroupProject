package hoang.com.auction_system_be.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemRejectRequest {

    @NotBlank(message = "Rejection reason is required")
    String rejectionReason;
}
