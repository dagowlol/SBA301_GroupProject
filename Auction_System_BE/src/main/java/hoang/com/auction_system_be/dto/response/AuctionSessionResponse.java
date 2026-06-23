package hoang.com.auction_system_be.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import hoang.com.auction_system_be.enums.SessionStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuctionSessionResponse {

    Long id;
    Long itemId;
    String itemName;
    String itemDescription;
    String itemImage;
    Long createdById;
    String createdByName;
    LocalDateTime startTime;
    LocalDateTime endTime;
    BigDecimal reservePrice;
    BigDecimal minimumIncrement;
    BigDecimal currentHighestBid;
    String currentWinnerName;
    SessionStatus status;
    int antiSnipeWindowSeconds;
    int antiSnipeExtensionSeconds;
    int bidCount;
    String cancellationReason;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
