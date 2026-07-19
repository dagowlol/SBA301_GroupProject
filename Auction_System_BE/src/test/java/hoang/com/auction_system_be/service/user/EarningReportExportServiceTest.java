package hoang.com.auction_system_be.service.user;

import hoang.com.auction_system_be.dto.response.EarningTransactionResponse;
import hoang.com.auction_system_be.enums.EarningExportFormat;
import hoang.com.auction_system_be.repository.AuctionSessionRepository;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EarningReportExportServiceTest {

    @Mock
    private AuctionSessionRepository auctionSessionRepository;

    @InjectMocks
    private EarningReportServiceImpl earningReportService;

    @Test
    void export_shouldReadTransactionsInCursorBatches() throws Exception {
        List<EarningTransactionResponse> firstBatch = new ArrayList<>();
        for (long id = 2_000; id >= 1_001; id--) {
            firstBatch.add(transaction(id));
        }
        List<EarningTransactionResponse> secondBatch = List.of(transaction(1_000));

        when(auctionSessionRepository.findOptimizedEarningTransactions(
                7L, false, 0L, "ALL", Pageable.ofSize(1_000)))
                .thenReturn(firstBatch);
        when(auctionSessionRepository.findOptimizedEarningTransactions(
                7L, true, 1_001L, "ALL", Pageable.ofSize(1_000)))
                .thenReturn(secondBatch);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        earningReportService.exportEarningTransactions(
                7L, "ALL", EarningExportFormat.EXCEL, outputStream);

        verify(auctionSessionRepository).findOptimizedEarningTransactions(
                7L, false, 0L, "ALL", Pageable.ofSize(1_000));
        verify(auctionSessionRepository).findOptimizedEarningTransactions(
                7L, true, 1_001L, "ALL", Pageable.ofSize(1_000));

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(outputStream.toByteArray()))) {
            assertThat(workbook.getSheet("Earning transactions").getLastRowNum()).isEqualTo(1_001);
        }
    }

    private EarningTransactionResponse transaction(long id) {
        return new EarningTransactionResponse(
                "INV-" + id,
                "Product " + id,
                LocalDateTime.of(2026, 7, 19, 10, 0),
                BigDecimal.valueOf(id),
                "Buyer",
                "SUCCESS",
                id);
    }
}
