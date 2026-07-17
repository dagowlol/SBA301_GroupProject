package hoang.com.auction_system_be.service.user;

import hoang.com.auction_system_be.dto.response.CursorPageResponse;
import hoang.com.auction_system_be.dto.response.EarningStatisticsResponse;
import hoang.com.auction_system_be.dto.response.EarningSummaryResponse;
import hoang.com.auction_system_be.dto.response.EarningTransactionResponse;
import hoang.com.auction_system_be.enums.EarningStatisticsRange;
import hoang.com.auction_system_be.repository.AuctionSessionRepository;
import hoang.com.auction_system_be.repository.projection.EarningRevenuePointProjection;
import hoang.com.auction_system_be.repository.projection.EarningStatusCountProjection;
import hoang.com.auction_system_be.repository.projection.EarningSummaryProjection;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class EarningReportServiceImpl implements EarningReportService {

    private static final DateTimeFormatter DAY_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    AuctionSessionRepository auctionSessionRepository;

    @Override
    @Transactional(readOnly = true)
    public EarningSummaryResponse getEarningSummary(Long userId) {
        EarningSummaryProjection summary = auctionSessionRepository.getEarningSummary(userId);

        if (summary == null) {
            return new EarningSummaryResponse(BigDecimal.ZERO, 0L, BigDecimal.ZERO);
        }

        BigDecimal totalRevenue = summary.getTotalRevenue() != null
                ? summary.getTotalRevenue()
                : BigDecimal.ZERO;
        long successfulProducts = summary.getSuccessfulProducts() != null
                ? summary.getSuccessfulProducts()
                : 0L;
        BigDecimal pendingAmount = summary.getPendingAmount() != null
                ? summary.getPendingAmount()
                : BigDecimal.ZERO;

        return new EarningSummaryResponse(totalRevenue, successfulProducts, pendingAmount);
    }

    @Override
    @Transactional(readOnly = true)
    public CursorPageResponse<EarningTransactionResponse> getEarningTransactions(Long userId, String cursorStr,
            int size, String status) {
        Pageable pageable = PageRequest.of(0, size + 1);

        Long cursor = 0L;
        boolean hasCursor = false;
        if (cursorStr != null && !cursorStr.trim().isEmpty()) {
            try {
                if (cursorStr.startsWith("INV-")) {
                    cursor = Long.parseLong(cursorStr.substring(4));
                } else {
                    cursor = Long.parseLong(cursorStr);
                }
                hasCursor = true;
            } catch (NumberFormatException e) {
                log.warn("Invalid cursor format: {}", cursorStr);
            }
        }

        String safeStatus = (status != null && !status.trim().isEmpty()) ? status.trim().toUpperCase() : "ALL";

        List<EarningTransactionResponse> transactions = auctionSessionRepository.findOptimizedEarningTransactions(
                userId, hasCursor, cursor, safeStatus, pageable);

        boolean hasNext = transactions.size() > size;
        if (hasNext) {
            transactions.remove(transactions.size() - 1);
        }

        Long nextCursor = null;
        if (!transactions.isEmpty()) {
            EarningTransactionResponse lastTx = transactions.get(transactions.size() - 1);
            nextCursor = lastTx.rawId(); // Record accessor returns Long for CursorPageResponse
        }

        return CursorPageResponse.<EarningTransactionResponse>builder()
                .content(transactions)
                .nextCursor(nextCursor)
                .hasNext(hasNext)
                .pageSize(size)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public EarningStatisticsResponse getEarningStatistics(Long userId, EarningStatisticsRange range) {
        EarningStatisticsRange safeRange = range != null ? range : EarningStatisticsRange.LAST_6_MONTHS;
        LocalDate today = LocalDate.now();
        boolean daily = safeRange == EarningStatisticsRange.LAST_7_DAYS
                || safeRange == EarningStatisticsRange.LAST_30_DAYS;

        LocalDate startDate = switch (safeRange) {
            case LAST_7_DAYS -> today.minusDays(6);
            case LAST_30_DAYS -> today.minusDays(29);
            case LAST_6_MONTHS -> today.withDayOfMonth(1).minusMonths(5);
            case LAST_12_MONTHS -> today.withDayOfMonth(1).minusMonths(11);
        };

        LocalDateTime fromDate = startDate.atStartOfDay();
        LocalDateTime toDate = today.plusDays(1).atStartOfDay();
        List<EarningRevenuePointProjection> rows = daily
                ? auctionSessionRepository.getDailyEarningRevenue(userId, fromDate, toDate)
                : auctionSessionRepository.getMonthlyEarningRevenue(userId, fromDate, toDate);

        Map<String, BigDecimal> revenueByPeriod = new HashMap<>();
        for (EarningRevenuePointProjection row : rows) {
            revenueByPeriod.put(
                    row.getPeriod(),
                    row.getRevenue() != null ? row.getRevenue() : BigDecimal.ZERO);
        }

        List<EarningStatisticsResponse.RevenuePoint> points = daily
                ? buildDailyPoints(startDate, today, revenueByPeriod)
                : buildMonthlyPoints(YearMonth.from(startDate), YearMonth.from(today), revenueByPeriod);

        EarningStatusCountProjection statusCounts = auctionSessionRepository.getEarningStatusCounts(
                userId, fromDate, toDate);
        long paidCount = statusCounts != null && statusCounts.getPaidCount() != null
                ? statusCounts.getPaidCount()
                : 0L;
        long pendingCount = statusCounts != null && statusCounts.getPendingCount() != null
                ? statusCounts.getPendingCount()
                : 0L;

        return new EarningStatisticsResponse(safeRange, points, paidCount, pendingCount);
    }

    private List<EarningStatisticsResponse.RevenuePoint> buildDailyPoints(
            LocalDate startDate,
            LocalDate endDate,
            Map<String, BigDecimal> revenueByPeriod) {
        List<EarningStatisticsResponse.RevenuePoint> points = new ArrayList<>();
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            String period = date.format(DAY_FORMATTER);
            points.add(new EarningStatisticsResponse.RevenuePoint(
                    period,
                    revenueByPeriod.getOrDefault(period, BigDecimal.ZERO)));
        }
        return points;
    }

    private List<EarningStatisticsResponse.RevenuePoint> buildMonthlyPoints(
            YearMonth startMonth,
            YearMonth endMonth,
            Map<String, BigDecimal> revenueByPeriod) {
        List<EarningStatisticsResponse.RevenuePoint> points = new ArrayList<>();
        for (YearMonth month = startMonth; !month.isAfter(endMonth); month = month.plusMonths(1)) {
            String period = month.format(MONTH_FORMATTER);
            points.add(new EarningStatisticsResponse.RevenuePoint(
                    period,
                    revenueByPeriod.getOrDefault(period, BigDecimal.ZERO)));
        }
        return points;
    }
}
