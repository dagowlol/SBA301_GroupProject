package hoang.com.auction_system_be.service.analytics;

import hoang.com.auction_system_be.dto.response.AuctionStatisticsResponse;

import java.time.LocalDate;

public interface AnalyticsService {
    AuctionStatisticsResponse getAuctionStatistics(LocalDate from, LocalDate to);
}
