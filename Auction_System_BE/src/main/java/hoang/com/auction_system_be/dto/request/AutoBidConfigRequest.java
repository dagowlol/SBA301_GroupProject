package hoang.com.auction_system_be.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AutoBidConfigRequest {
    BigDecimal maxBidAmount;
    BigDecimal bidIncrement;
    boolean isActive;
}
