package hoang.com.auction_system_be.dto.response;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;
import java.util.List;

@Value
@Builder
public class AuctionStatisticsResponse {
    LocalDate from;
    LocalDate to;
    AnalyticsSummaryResponse summary;
    List<RevenueTrendPointResponse> revenueTrend;
    List<CategoryAuctionSuccessResponse> categorySuccess;
}
