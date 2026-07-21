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
public class AuctionSessionListResponse {

    private Long id;
    private Long itemId;
    private String itemName;
    private String itemDescription;
    private BigDecimal reservePrice;
    private BigDecimal currentHighestBid;
    private String itemImage;
    private SessionStatus status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
