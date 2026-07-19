package hoang.com.auction_system_be.repository.projection;

import java.math.BigDecimal;

public interface EarningRevenuePointProjection {
    String getPeriod();

    BigDecimal getRevenue();
}
