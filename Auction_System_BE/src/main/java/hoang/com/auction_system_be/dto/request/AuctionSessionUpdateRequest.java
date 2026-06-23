package hoang.com.auction_system_be.dto.request;

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
public class AuctionSessionUpdateRequest {

    LocalDateTime endTime;

    BigDecimal reservePrice;

    BigDecimal minimumIncrement;

    Integer antiSnipeWindowSeconds;

    Integer antiSnipeExtensionSeconds;

    SessionStatus status;

    String cancellationReason;
}
