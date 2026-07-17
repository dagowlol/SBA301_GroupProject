package hoang.com.auction_system_be.service.analytics;

import hoang.com.auction_system_be.dto.response.*;
import hoang.com.auction_system_be.enums.BidStatus;
import hoang.com.auction_system_be.enums.SessionStatus;
import hoang.com.auction_system_be.repository.AuctionSessionRepository;
import hoang.com.auction_system_be.repository.BidRepository;
import hoang.com.auction_system_be.repository.PaymentRepository;
import hoang.com.auction_system_be.repository.UserRepository;
import hoang.com.auction_system_be.repository.projection.AnalyticsRevenuePointProjection;
import hoang.com.auction_system_be.repository.projection.CategoryAuctionSuccessProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalyticsServiceImpl implements AnalyticsService {
    private final PaymentRepository paymentRepository;
    private final AuctionSessionRepository auctionSessionRepository;
    private final BidRepository bidRepository;
    private final UserRepository userRepository;

    @Override
    public AuctionStatisticsResponse getAuctionStatistics(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("Both from and to dates are required");
        }
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("From date must not be after to date");
        }

        LocalDateTime fromDate = from.atStartOfDay();
        LocalDateTime toExclusive = to.plusDays(1).atStartOfDay();
        BigDecimal revenue = paymentRepository.sumPaidRevenue(fromDate, toExclusive);

        AnalyticsSummaryResponse summary = AnalyticsSummaryResponse.builder()
                .revenue(revenue == null ? BigDecimal.ZERO : revenue)
                .runningSessions(auctionSessionRepository.countRunningSessionsInRange(
                        SessionStatus.ACTIVE, fromDate, toExclusive))
                .totalBids(bidRepository.countByBidTimestampGreaterThanEqualAndBidTimestampLessThanAndStatusNot(
                        fromDate, toExclusive, BidStatus.CANCELLED))
                .newUsers(userRepository.countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(fromDate, toExclusive))
                .build();

        List<RevenueTrendPointResponse> revenueTrend = paymentRepository
                .getDailyPaidRevenue(fromDate, toExclusive).stream()
                .map(this::toRevenuePoint)
                .toList();

        List<CategoryAuctionSuccessProjection> categoryRows = auctionSessionRepository
                .getSuccessfulAuctionsByCategory(fromDate, toExclusive);
        long totalSuccessful = categoryRows.stream()
                .mapToLong(row -> valueOrZero(row.getSuccessfulAuctions()))
                .sum();
        List<CategoryAuctionSuccessResponse> categorySuccess = categoryRows.stream()
                .map(row -> toCategorySuccess(row, totalSuccessful))
                .toList();

        return AuctionStatisticsResponse.builder()
                .from(from)
                .to(to)
                .summary(summary)
                .revenueTrend(revenueTrend)
                .categorySuccess(categorySuccess)
                .build();
    }

    private RevenueTrendPointResponse toRevenuePoint(AnalyticsRevenuePointProjection point) {
        return RevenueTrendPointResponse.builder()
                .date(point.getPeriod())
                .revenue(point.getRevenue() == null ? BigDecimal.ZERO : point.getRevenue())
                .build();
    }

    private CategoryAuctionSuccessResponse toCategorySuccess(CategoryAuctionSuccessProjection row, long total) {
        long count = valueOrZero(row.getSuccessfulAuctions());
        double percentage = total == 0 ? 0 : BigDecimal.valueOf(count * 100.0 / total)
                .setScale(1, RoundingMode.HALF_UP)
                .doubleValue();
        return CategoryAuctionSuccessResponse.builder()
                .categoryId(row.getCategoryId())
                .categoryName(row.getCategoryName())
                .successfulAuctions(count)
                .percentage(percentage)
                .build();
    }

    private long valueOrZero(Long value) {
        return value == null ? 0 : value;
    }
}
