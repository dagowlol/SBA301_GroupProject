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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.annotation.Cacheable;

import java.math.BigDecimal;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuctionStatisticServiceImplTest {

    @Mock
    PaymentRepository paymentRepository;
    @Mock
    AuctionSessionRepository auctionSessionRepository;
    @Mock
    BidRepository bidRepository;
    @Mock
    UserRepository userRepository;

    @InjectMocks
    AuctionStatisticServiceImpl auctionStatisticService;

    @Test
    void getOverview_shouldAggregateAllRequiredMetrics() {
        LocalDateTime from = LocalDateTime.of(2026, 7, 1, 0, 0);
        LocalDateTime toExclusive = LocalDateTime.of(2026, 8, 1, 0, 0);
        when(paymentRepository.sumPaidRevenue(from, toExclusive)).thenReturn(new BigDecimal("88700000"));
        when(auctionSessionRepository.countCompletedSessionsInRange(from, toExclusive)).thenReturn(12L);
        when(bidRepository.countByBidTimestampGreaterThanEqualAndBidTimestampLessThanAndStatusNot(
                from, toExclusive, BidStatus.CANCELLED)).thenReturn(245L);
        when(userRepository.countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(from, toExclusive)).thenReturn(18L);

        AuctionOverviewResponse result = auctionStatisticService.getOverview("2026-07-01", "2026-07-31");

        assertEquals(new BigDecimal("88700000"), result.getTotalRevenue());
        assertEquals(12L, result.getCompletedSessions());
        assertEquals(245L, result.getTotalBids());
        assertEquals(18L, result.getNewUsers());
    }

    @Test
    void getOverview_shouldRejectInvalidDateFormatWithAppException() {
        AppException error = assertThrows(AppException.class,
                () -> auctionStatisticService.getOverview("2026/07/01", "2026-07-31"));

        assertEquals(ErrorCode.INVALID_ANALYTICS_DATE_FORMAT, error.getErrorCode());
        verifyNoInteractions(paymentRepository, auctionSessionRepository, bidRepository, userRepository);
    }

    @Test
    void getOverview_shouldRejectReversedDateRangeWithAppException() {
        AppException error = assertThrows(AppException.class,
                () -> auctionStatisticService.getOverview("2026-08-01", "2026-07-31"));

        assertEquals(ErrorCode.INVALID_ANALYTICS_DATE_RANGE, error.getErrorCode());
        verifyNoInteractions(paymentRepository, auctionSessionRepository, bidRepository, userRepository);
    }

    @Test
    void getRevenueChart_shouldUseRequestedDatabaseAggregation() {
        AnalyticsRevenuePointProjection point = mock(AnalyticsRevenuePointProjection.class);
        when(point.getPeriod()).thenReturn("2026-07");
        when(point.getRevenue()).thenReturn(new BigDecimal("88700000"));
        when(paymentRepository.getAllPaidRevenueByMonth()).thenReturn(List.of(point));

        List<RevenueChartPointResponse> result = auctionStatisticService.getRevenueChart(AnalyticsPeriod.MONTHLY);

        assertEquals(1, result.size());
        assertEquals("2026-07", result.getFirst().getPeriod());
        assertEquals(new BigDecimal("88700000"), result.getFirst().getRevenue());
        verify(paymentRepository).getAllPaidRevenueByMonth();
        verify(paymentRepository, never()).getAllPaidRevenueByDay();
        verify(paymentRepository, never()).getAllPaidRevenueByWeek();
    }

    @Test
    void getRevenueChart_shouldDeclareRequiredCacheContract() throws Exception {
        Method method = AuctionStatisticServiceImpl.class
                .getMethod("getRevenueChart", AnalyticsPeriod.class);
        Cacheable cacheable = method.getAnnotation(Cacheable.class);

        assertNotNull(cacheable);
        assertArrayEquals(new String[]{"statisticsCache"}, cacheable.value());
        assertEquals("#period", cacheable.key());
    }
}
