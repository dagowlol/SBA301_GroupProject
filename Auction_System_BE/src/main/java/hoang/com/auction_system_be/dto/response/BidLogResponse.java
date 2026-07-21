package hoang.com.auction_system_be.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
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
public class BidLogResponse {

    Long bidId;
    Long sessionId;
    Long userId;
    String bidderName;
    BigDecimal amount;
    LocalDateTime bidTime;
    Boolean isSuspicious;
    String status;
}
