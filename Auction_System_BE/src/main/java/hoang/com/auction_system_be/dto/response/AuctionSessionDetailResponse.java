package hoang.com.auction_system_be.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import hoang.com.auction_system_be.enums.SessionStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuctionSessionDetailResponse {

    Long sessionId;
    Long itemId;
    String itemName;
    String itemImage;
    String itemDescription;
    LocalDateTime endTime;
    BigDecimal currentPrice;
    String currentWinnerName;
    Long winnerId;
    String winnerName;
    List<BidLogResponse> bidLogs;
    SessionStatus status;
    BigDecimal minimumIncrement;
    BigDecimal reservePrice;
}
