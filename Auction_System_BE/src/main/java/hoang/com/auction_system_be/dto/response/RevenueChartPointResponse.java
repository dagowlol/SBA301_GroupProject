package hoang.com.auction_system_be.dto.response;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class RevenueChartPointResponse {
    String period;
    BigDecimal revenue;
}
