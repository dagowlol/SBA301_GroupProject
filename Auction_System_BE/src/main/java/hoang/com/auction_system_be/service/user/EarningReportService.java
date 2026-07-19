package hoang.com.auction_system_be.service.user;

import hoang.com.auction_system_be.dto.response.CursorPageResponse;
import hoang.com.auction_system_be.dto.response.EarningSummaryResponse;
import hoang.com.auction_system_be.dto.response.EarningStatisticsResponse;
import hoang.com.auction_system_be.dto.response.EarningTransactionResponse;
import hoang.com.auction_system_be.enums.EarningStatisticsRange;
import hoang.com.auction_system_be.enums.EarningExportFormat;
import java.io.IOException;
import java.io.OutputStream;

public interface EarningReportService {
    EarningSummaryResponse getEarningSummary(Long userId);

    EarningStatisticsResponse getEarningStatistics(Long userId, EarningStatisticsRange range);

    CursorPageResponse<EarningTransactionResponse> getEarningTransactions(Long userId, String cursor, int size, String status);

    void exportEarningTransactions(
            Long userId,
            String status,
            EarningExportFormat format,
            OutputStream outputStream) throws IOException;
}
