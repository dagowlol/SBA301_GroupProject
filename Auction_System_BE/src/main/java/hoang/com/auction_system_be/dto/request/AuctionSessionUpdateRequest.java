package hoang.com.auction_system_be.dto.request;

import hoang.com.auction_system_be.enums.SessionStatus;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuctionSessionUpdateRequest {

    @Future(message = "End time must be in the future")
    LocalDateTime endTime;

    @Min(value = 1, message = "Reserve price must be greater than 0")
    BigDecimal reservePrice;

    @Min(value = 1, message = "Minimum increment must be greater than 0")
    BigDecimal minimumIncrement;

    @Min(value = 1, message = "Anti-snipe window must be greater than 0")
    Integer antiSnipeWindowSeconds;

    @Min(value = 1, message = "Anti-snipe extension must be greater than 0")
    Integer antiSnipeExtensionSeconds;

    SessionStatus status;

    @Size(max = 500, message = "Cancellation reason cannot exceed 500 characters")
    String cancellationReason;
}
