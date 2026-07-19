package hoang.com.auction_system_be.controller;

import hoang.com.auction_system_be.dto.response.ApiResponse;
import hoang.com.auction_system_be.dto.response.AuctionOverviewResponse;
import hoang.com.auction_system_be.dto.response.RevenueChartPointResponse;
import hoang.com.auction_system_be.enums.AnalyticsPeriod;
import hoang.com.auction_system_be.service.analytics.AuctionStatisticService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
public class AuctionStatisticController {
    private final AuctionStatisticService auctionStatisticService;

    @GetMapping("/overview")
    public ResponseEntity<ApiResponse<AuctionOverviewResponse>> getOverview(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        return ResponseEntity.ok(ApiResponse.success(
                auctionStatisticService.getOverview(startDate, endDate)));
    }

    @GetMapping("/revenue-chart")
    public ResponseEntity<ApiResponse<List<RevenueChartPointResponse>>> getRevenueChart(
            @RequestParam String period) {
        return ResponseEntity.ok(ApiResponse.success(
                auctionStatisticService.getRevenueChart(AnalyticsPeriod.from(period))));
    }
}
