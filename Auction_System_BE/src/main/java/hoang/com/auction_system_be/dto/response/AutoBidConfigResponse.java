package hoang.com.auction_system_be.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AutoBidConfigResponse {
    Long id;
    Long sessionId;
    Long userId;
    BigDecimal maxBidAmount;
    BigDecimal bidIncrement;
    boolean isActive;
}
