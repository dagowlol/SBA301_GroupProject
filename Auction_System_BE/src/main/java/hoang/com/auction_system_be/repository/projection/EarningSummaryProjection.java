package hoang.com.auction_system_be.repository.projection;

import java.math.BigDecimal;

public interface EarningSummaryProjection {
    BigDecimal getTotalRevenue();

    Long getSuccessfulProducts();

    BigDecimal getPendingAmount();
}
