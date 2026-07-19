package hoang.com.auction_system_be.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class AuctionOverviewResponse {
    @JsonProperty("total_revenue")
    BigDecimal totalRevenue;

    @JsonProperty("completed_sessions")
    long completedSessions;

    @JsonProperty("total_bids")
    long totalBids;

    @JsonProperty("new_users")
    long newUsers;
}
