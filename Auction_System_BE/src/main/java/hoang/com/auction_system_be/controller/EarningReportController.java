package hoang.com.auction_system_be.controller;

import hoang.com.auction_system_be.dto.response.ApiResponse;
import hoang.com.auction_system_be.dto.response.CursorPageResponse;
import hoang.com.auction_system_be.dto.response.EarningSummaryResponse;
import hoang.com.auction_system_be.dto.response.EarningStatisticsResponse;
import hoang.com.auction_system_be.dto.response.EarningTransactionResponse;
import hoang.com.auction_system_be.enums.EarningStatisticsRange;
import hoang.com.auction_system_be.enums.EarningExportFormat;
import hoang.com.auction_system_be.service.user.EarningReportService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/users/{userId}")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EarningReportController {

    EarningReportService earningReportService;

    @GetMapping("/earning-report")
    @PreAuthorize("#userId == authentication.principal.id")
    public ResponseEntity<ApiResponse<EarningSummaryResponse>> getEarningSummary(
            @PathVariable Long userId) {
        EarningSummaryResponse response = earningReportService.getEarningSummary(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/earning-statistics")
    @PreAuthorize("#userId == authentication.principal.id")
    public ResponseEntity<ApiResponse<EarningStatisticsResponse>> getEarningStatistics(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "LAST_6_MONTHS") EarningStatisticsRange range) {
        EarningStatisticsResponse response = earningReportService.getEarningStatistics(userId, range);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/earning-transactions")
    @PreAuthorize("#userId == authentication.principal.id")
    public ResponseEntity<ApiResponse<CursorPageResponse<EarningTransactionResponse>>> getEarningTransactions(
            @PathVariable Long userId,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status) {

        CursorPageResponse<EarningTransactionResponse> response = earningReportService.getEarningTransactions(userId,
                cursor, size, status);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/earning-transactions/export")
    @PreAuthorize("#userId == authentication.principal.id")
    public ResponseEntity<StreamingResponseBody> exportEarningTransactions(
            @PathVariable Long userId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "EXCEL") EarningExportFormat format) {
        String fileName = "earning-transactions-" + LocalDate.now() + "." + format.getExtension();
        StreamingResponseBody body = outputStream ->
                earningReportService.exportEarningTransactions(userId, status, format, outputStream);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(format.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(fileName, StandardCharsets.UTF_8)
                        .build()
                        .toString())
                .body(body);
    }
}
