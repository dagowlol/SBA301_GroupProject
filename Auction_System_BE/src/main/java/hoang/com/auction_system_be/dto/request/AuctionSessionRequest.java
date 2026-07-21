package hoang.com.auction_system_be.dto.request;

import hoang.com.auction_system_be.validation.FutureMinutes;
import hoang.com.auction_system_be.validation.ValidAuctionTimeline;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@ValidAuctionTimeline
public class AuctionSessionRequest {

    @NotNull(message = "Item ID is required")
    Long itemId;

    @NotNull(message = "Start time is required")
    @FutureMinutes(minMinutes = 5, message = "Start time must be at least 5 minutes from now (scheduler buffer)")
    LocalDateTime startTime;

    @NotNull(message = "End time is required")
    LocalDateTime endTime;

    @NotNull(message = "Reserve price is required")
    @Min(value = 1, message = "Reserve price must be greater than 0")
    BigDecimal reservePrice;

    @NotNull(message = "Minimum increment is required")
    @Min(value = 1, message = "Minimum increment must be greater than 0")
    BigDecimal minimumIncrement;

    Integer antiSnipeWindowSeconds;

    Integer antiSnipeExtensionSeconds;
}
