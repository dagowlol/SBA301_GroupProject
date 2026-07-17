package hoang.com.auction_system_be.controller;

import hoang.com.auction_system_be.dto.response.AuctionOverviewResponse;
import hoang.com.auction_system_be.dto.response.RevenueChartPointResponse;
import hoang.com.auction_system_be.enums.AnalyticsPeriod;
import hoang.com.auction_system_be.exception.AppException;
import hoang.com.auction_system_be.exception.ErrorCode;
import hoang.com.auction_system_be.service.analytics.AuctionStatisticService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class AuctionStatisticControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    AuctionStatisticService auctionStatisticService;

    @Test
    @WithMockUser(authorities = "ADMIN")
    void getOverview_shouldReturnExpectedContractForAdmin() throws Exception {
        when(auctionStatisticService.getOverview("2026-07-01", "2026-07-31"))
                .thenReturn(AuctionOverviewResponse.builder()
                        .totalRevenue(new BigDecimal("88700000"))
                        .completedSessions(12)
                        .totalBids(245)
                        .newUsers(18)
                        .build());

        mockMvc.perform(get("/api/v1/analytics/overview")
                        .param("startDate", "2026-07-01")
                        .param("endDate", "2026-07-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.result.total_revenue").value(88700000))
                .andExpect(jsonPath("$.result.completed_sessions").value(12))
                .andExpect(jsonPath("$.result.total_bids").value(245))
                .andExpect(jsonPath("$.result.new_users").value(18));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void getRevenueChart_shouldAcceptCaseInsensitivePeriod() throws Exception {
        when(auctionStatisticService.getRevenueChart(AnalyticsPeriod.WEEKLY))
                .thenReturn(List.of(RevenueChartPointResponse.builder()
                        .period("2026-07-06")
                        .revenue(new BigDecimal("12500000"))
                        .build()));

        mockMvc.perform(get("/api/v1/analytics/revenue-chart")
                        .param("period", "weekly"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result[0].period").value("2026-07-06"))
                .andExpect(jsonPath("$.result[0].revenue").value(12500000));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void getRevenueChart_shouldRejectUnsupportedPeriod() throws Exception {
        mockMvc.perform(get("/api/v1/analytics/revenue-chart")
                        .param("period", "YEARLY"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(9003))
                .andExpect(jsonPath("$.message").value("Period must be DAILY, WEEKLY, or MONTHLY"));

        verifyNoInteractions(auctionStatisticService);
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void getOverview_shouldReturnAnalyticsErrorCodeForInvalidDateFormat() throws Exception {
        when(auctionStatisticService.getOverview("07/01/2026", "2026-07-31"))
                .thenThrow(new AppException(ErrorCode.INVALID_ANALYTICS_DATE_FORMAT));

        mockMvc.perform(get("/api/v1/analytics/overview")
                        .param("startDate", "07/01/2026")
                        .param("endDate", "2026-07-31"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(9001))
                .andExpect(jsonPath("$.message").value("Dates must use ISO format yyyy-MM-dd"));
    }

    @Test
    @WithMockUser(authorities = "USER")
    void analyticsEndpoints_shouldReturnForbiddenForRegularUser() throws Exception {
        mockMvc.perform(get("/api/v1/analytics/overview")
                        .param("startDate", "2026-07-01")
                        .param("endDate", "2026-07-31"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/analytics/revenue-chart")
                        .param("period", "DAILY"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(auctionStatisticService);
    }

    @Test
    @WithMockUser(authorities = "AUCTION_MANAGER")
    void analyticsEndpoints_shouldReturnForbiddenForAuctionManager() throws Exception {
        mockMvc.perform(get("/api/v1/analytics/overview")
                        .param("startDate", "2026-07-01")
                        .param("endDate", "2026-07-31"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(auctionStatisticService);
    }
}
