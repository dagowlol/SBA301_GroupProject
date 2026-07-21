package hoang.com.auction_system_be.dto.response;

import java.math.BigDecimal;

public record EarningSummaryResponse(
    BigDecimal totalRevenue,
    long successfulProducts,
    BigDecimal pendingAmount
) {
}
