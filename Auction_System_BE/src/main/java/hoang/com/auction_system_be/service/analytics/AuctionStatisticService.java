package hoang.com.auction_system_be.service.analytics;

import hoang.com.auction_system_be.dto.response.AuctionOverviewResponse;
import hoang.com.auction_system_be.dto.response.RevenueChartPointResponse;
import hoang.com.auction_system_be.enums.AnalyticsPeriod;

import java.util.List;

public interface AuctionStatisticService {
    AuctionOverviewResponse getOverview(String startDate, String endDate);

    List<RevenueChartPointResponse> getRevenueChart(AnalyticsPeriod period);
}
