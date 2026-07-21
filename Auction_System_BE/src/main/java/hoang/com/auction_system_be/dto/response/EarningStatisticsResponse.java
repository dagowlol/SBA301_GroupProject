package hoang.com.auction_system_be.dto.response;

import hoang.com.auction_system_be.enums.EarningStatisticsRange;

import java.math.BigDecimal;
import java.util.List;

public record EarningStatisticsResponse(
        EarningStatisticsRange range,
        List<RevenuePoint> revenueByPeriod,
        long paidCount,
        long pendingCount
) {
    public record RevenuePoint(
            String period,
            BigDecimal revenue
    ) {
    }
}
