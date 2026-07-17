package hoang.com.auction_system_be.service.analytics;

import hoang.com.auction_system_be.dto.response.AuctionStatisticsResponse;
import hoang.com.auction_system_be.enums.BidStatus;
import hoang.com.auction_system_be.enums.SessionStatus;
import hoang.com.auction_system_be.repository.AuctionSessionRepository;
import hoang.com.auction_system_be.repository.BidRepository;
import hoang.com.auction_system_be.repository.PaymentRepository;
import hoang.com.auction_system_be.repository.UserRepository;
import hoang.com.auction_system_be.repository.projection.AnalyticsRevenuePointProjection;
import hoang.com.auction_system_be.repository.projection.CategoryAuctionSuccessProjection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceImplTest {

    @Mock PaymentRepository paymentRepository;
    @Mock AuctionSessionRepository auctionSessionRepository;
    @Mock BidRepository bidRepository;
    @Mock UserRepository userRepository;

    @InjectMocks AnalyticsServiceImpl analyticsService;

    @Test
    void getAuctionStatistics_shouldAggregateAndMapAllSections() {
        LocalDate from = LocalDate.of(2026, 7, 1);
        LocalDate to = LocalDate.of(2026, 7, 7);
        LocalDateTime fromDate = from.atStartOfDay();
        LocalDateTime toExclusive = to.plusDays(1).atStartOfDay();

        when(paymentRepository.sumPaidRevenue(fromDate, toExclusive)).thenReturn(new BigDecimal("2500000.00"));
        when(auctionSessionRepository.countRunningSessionsInRange(SessionStatus.ACTIVE, fromDate, toExclusive))
                .thenReturn(4L);
        when(bidRepository.countByBidTimestampGreaterThanEqualAndBidTimestampLessThanAndStatusNot(
                fromDate, toExclusive, BidStatus.CANCELLED)).thenReturn(125L);
        when(userRepository.countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(fromDate, toExclusive))
                .thenReturn(9L);
        List<AnalyticsRevenuePointProjection> revenuePoints = List.of(
                revenuePoint("2026-07-01", "1000000"),
                revenuePoint("2026-07-02", "1500000"));
        List<CategoryAuctionSuccessProjection> categoryPoints = List.of(
                categoryPoint(1L, "Watches", 3L),
                categoryPoint(2L, "Art", 1L));
        when(paymentRepository.getDailyPaidRevenue(fromDate, toExclusive)).thenReturn(revenuePoints);
        when(auctionSessionRepository.getSuccessfulAuctionsByCategory(fromDate, toExclusive))
                .thenReturn(categoryPoints);

        AuctionStatisticsResponse result = analyticsService.getAuctionStatistics(from, to);

        assertEquals(from, result.getFrom());
        assertEquals(to, result.getTo());
        assertEquals(new BigDecimal("2500000.00"), result.getSummary().getRevenue());
        assertEquals(4L, result.getSummary().getRunningSessions());
        assertEquals(125L, result.getSummary().getTotalBids());
        assertEquals(9L, result.getSummary().getNewUsers());
        assertEquals(2, result.getRevenueTrend().size());
        assertEquals("2026-07-01", result.getRevenueTrend().getFirst().getDate());
        assertEquals(new BigDecimal("1000000"), result.getRevenueTrend().getFirst().getRevenue());
        assertEquals(75.0, result.getCategorySuccess().getFirst().getPercentage());
        assertEquals(25.0, result.getCategorySuccess().get(1).getPercentage());
    }

    @Test
    void getAuctionStatistics_shouldReturnZerosAndEmptyListsWhenNoData() {
        LocalDate from = LocalDate.of(2026, 7, 1);
        LocalDate to = LocalDate.of(2026, 7, 1);
        LocalDateTime fromDate = from.atStartOfDay();
        LocalDateTime toExclusive = to.plusDays(1).atStartOfDay();

        when(paymentRepository.sumPaidRevenue(fromDate, toExclusive)).thenReturn(null);
        when(paymentRepository.getDailyPaidRevenue(fromDate, toExclusive)).thenReturn(List.of());
        when(auctionSessionRepository.getSuccessfulAuctionsByCategory(fromDate, toExclusive)).thenReturn(List.of());

        AuctionStatisticsResponse result = analyticsService.getAuctionStatistics(from, to);

        assertEquals(BigDecimal.ZERO, result.getSummary().getRevenue());
        assertEquals(0L, result.getSummary().getRunningSessions());
        assertEquals(0L, result.getSummary().getTotalBids());
        assertEquals(0L, result.getSummary().getNewUsers());
        assertTrue(result.getRevenueTrend().isEmpty());
        assertTrue(result.getCategorySuccess().isEmpty());
    }

    @Test
    void getAuctionStatistics_shouldRoundCategoryPercentageToOneDecimal() {
        LocalDate date = LocalDate.of(2026, 7, 1);
        LocalDateTime fromDate = date.atStartOfDay();
        LocalDateTime toExclusive = date.plusDays(1).atStartOfDay();
        when(paymentRepository.getDailyPaidRevenue(fromDate, toExclusive)).thenReturn(List.of());
        List<CategoryAuctionSuccessProjection> categoryPoints = List.of(
                categoryPoint(1L, "Watches", 1L),
                categoryPoint(2L, "Art", 2L));
        when(auctionSessionRepository.getSuccessfulAuctionsByCategory(fromDate, toExclusive))
                .thenReturn(categoryPoints);

        AuctionStatisticsResponse result = analyticsService.getAuctionStatistics(date, date);

        assertEquals(33.3, result.getCategorySuccess().getFirst().getPercentage());
        assertEquals(66.7, result.getCategorySuccess().get(1).getPercentage());
    }

    @Test
    void getAuctionStatistics_shouldRejectMissingDates() {
        assertThrows(IllegalArgumentException.class,
                () -> analyticsService.getAuctionStatistics(null, LocalDate.now()));
        assertThrows(IllegalArgumentException.class,
                () -> analyticsService.getAuctionStatistics(LocalDate.now(), null));
        verifyNoInteractions(paymentRepository, auctionSessionRepository, bidRepository, userRepository);
    }

    @Test
    void getAuctionStatistics_shouldRejectReversedDateRange() {
        LocalDate from = LocalDate.of(2026, 7, 8);
        LocalDate to = LocalDate.of(2026, 7, 1);

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> analyticsService.getAuctionStatistics(from, to));

        assertEquals("From date must not be after to date", error.getMessage());
        verifyNoInteractions(paymentRepository, auctionSessionRepository, bidRepository, userRepository);
    }

    private AnalyticsRevenuePointProjection revenuePoint(String period, String revenue) {
        AnalyticsRevenuePointProjection point = mock(AnalyticsRevenuePointProjection.class);
        when(point.getPeriod()).thenReturn(period);
        when(point.getRevenue()).thenReturn(new BigDecimal(revenue));
        return point;
    }

    private CategoryAuctionSuccessProjection categoryPoint(Long id, String name, Long count) {
        CategoryAuctionSuccessProjection point = mock(CategoryAuctionSuccessProjection.class);
        when(point.getCategoryId()).thenReturn(id);
        when(point.getCategoryName()).thenReturn(name);
        when(point.getSuccessfulAuctions()).thenReturn(count);
        return point;
    }
}
