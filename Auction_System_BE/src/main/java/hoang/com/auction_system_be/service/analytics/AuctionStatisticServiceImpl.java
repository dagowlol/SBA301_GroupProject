package hoang.com.auction_system_be.service.analytics;

import hoang.com.auction_system_be.dto.response.AuctionOverviewResponse;
import hoang.com.auction_system_be.dto.response.RevenueChartPointResponse;
import hoang.com.auction_system_be.enums.AnalyticsPeriod;
import hoang.com.auction_system_be.enums.BidStatus;
import hoang.com.auction_system_be.exception.AppException;
import hoang.com.auction_system_be.exception.ErrorCode;
import hoang.com.auction_system_be.repository.AuctionSessionRepository;
import hoang.com.auction_system_be.repository.BidRepository;
import hoang.com.auction_system_be.repository.PaymentRepository;
import hoang.com.auction_system_be.repository.UserRepository;
import hoang.com.auction_system_be.repository.projection.AnalyticsRevenuePointProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuctionStatisticServiceImpl implements AuctionStatisticService {
    private final PaymentRepository paymentRepository;
    private final AuctionSessionRepository auctionSessionRepository;
    private final BidRepository bidRepository;
    private final UserRepository userRepository;

    @Override
    public AuctionOverviewResponse getOverview(String startDate, String endDate) {
        DateRange range = parseRange(startDate, endDate);
        BigDecimal revenue = paymentRepository.sumPaidRevenue(range.from(), range.toExclusive());

        return AuctionOverviewResponse.builder()
                .totalRevenue(revenue == null ? BigDecimal.ZERO : revenue)
                .completedSessions(auctionSessionRepository.countCompletedSessionsInRange(
                        range.from(), range.toExclusive()))
                .totalBids(bidRepository.countByBidTimestampGreaterThanEqualAndBidTimestampLessThanAndStatusNot(
                        range.from(), range.toExclusive(), BidStatus.CANCELLED))
                .newUsers(userRepository.countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                        range.from(), range.toExclusive()))
                .build();
    }

    @Override
    @Cacheable(value = "statisticsCache", key = "#period")
    public List<RevenueChartPointResponse> getRevenueChart(AnalyticsPeriod period) {
        if (period == null) {
            throw new AppException(ErrorCode.INVALID_ANALYTICS_PERIOD);
        }

        List<AnalyticsRevenuePointProjection> points = switch (period) {
            case DAILY -> paymentRepository.getAllPaidRevenueByDay();
            case WEEKLY -> paymentRepository.getAllPaidRevenueByWeek();
            case MONTHLY -> paymentRepository.getAllPaidRevenueByMonth();
        };

        return points.stream()
                .map(point -> RevenueChartPointResponse.builder()
                        .period(point.getPeriod())
                        .revenue(point.getRevenue() == null ? BigDecimal.ZERO : point.getRevenue())
                        .build())
                .toList();
    }

    private DateRange parseRange(String startDate, String endDate) {
        final LocalDate start;
        final LocalDate end;

        try {
            start = LocalDate.parse(startDate);
            end = LocalDate.parse(endDate);
        } catch (DateTimeParseException | NullPointerException exception) {
            throw new AppException(ErrorCode.INVALID_ANALYTICS_DATE_FORMAT);
        }

        if (start.isAfter(end)) {
            throw new AppException(ErrorCode.INVALID_ANALYTICS_DATE_RANGE);
        }

        return new DateRange(start.atStartOfDay(), end.plusDays(1).atStartOfDay());
    }

    private record DateRange(LocalDateTime from, LocalDateTime toExclusive) {
    }
}
