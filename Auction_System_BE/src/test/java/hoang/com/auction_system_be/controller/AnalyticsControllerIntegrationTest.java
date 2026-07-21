package hoang.com.auction_system_be.controller;

import hoang.com.auction_system_be.dto.response.AnalyticsSummaryResponse;
import hoang.com.auction_system_be.dto.response.AuctionStatisticsResponse;
import hoang.com.auction_system_be.dto.response.CategoryAuctionSuccessResponse;
import hoang.com.auction_system_be.dto.response.RevenueTrendPointResponse;
import hoang.com.auction_system_be.service.analytics.AnalyticsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class AnalyticsControllerIntegrationTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean AnalyticsService analyticsService;

    @Test
    @WithMockUser(authorities = "ADMIN")
    void getAuctionStatistics_shouldReturnExpectedContractForAdmin() throws Exception {
        LocalDate from = LocalDate.of(2026, 7, 1);
        LocalDate to = LocalDate.of(2026, 7, 7);
        when(analyticsService.getAuctionStatistics(from, to)).thenReturn(response(from, to));

        mockMvc.perform(get("/api/v1/admin/analytics/auction-statistics")
                        .param("from", "2026-07-01")
                        .param("to", "2026-07-07"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.result.from").value("2026-07-01"))
                .andExpect(jsonPath("$.result.to").value("2026-07-07"))
                .andExpect(jsonPath("$.result.summary.revenue").value(2500000))
                .andExpect(jsonPath("$.result.summary.runningSessions").value(4))
                .andExpect(jsonPath("$.result.summary.totalBids").value(125))
                .andExpect(jsonPath("$.result.summary.newUsers").value(9))
                .andExpect(jsonPath("$.result.revenueTrend[0].date").value("2026-07-01"))
                .andExpect(jsonPath("$.result.categorySuccess[0].categoryName").value("Watches"))
                .andExpect(jsonPath("$.result.categorySuccess[0].percentage").value(100.0));

        verify(analyticsService).getAuctionStatistics(from, to);
    }

    @Test
    @WithMockUser(authorities = "AUCTION_MANAGER")
    void getAuctionStatistics_shouldRejectAuctionManager() throws Exception {
        mockMvc.perform(get("/api/v1/admin/analytics/auction-statistics")
                        .param("from", "2026-07-01")
                        .param("to", "2026-07-01"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(analyticsService);
    }

    @Test
    @WithMockUser(authorities = "USER")
    void getAuctionStatistics_shouldRejectRegularUser() throws Exception {
        mockMvc.perform(get("/api/v1/admin/analytics/auction-statistics")
                        .param("from", "2026-07-01")
                        .param("to", "2026-07-07"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(analyticsService);
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void getAuctionStatistics_shouldReturnBadRequestWhenDateIsMissing() throws Exception {
        mockMvc.perform(get("/api/v1/admin/analytics/auction-statistics")
                        .param("from", "2026-07-01"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(analyticsService);
    }

    private AuctionStatisticsResponse response(LocalDate from, LocalDate to) {
        return AuctionStatisticsResponse.builder()
                .from(from)
                .to(to)
                .summary(AnalyticsSummaryResponse.builder()
                        .revenue(new BigDecimal("2500000"))
                        .runningSessions(4)
                        .totalBids(125)
                        .newUsers(9)
                        .build())
                .revenueTrend(List.of(RevenueTrendPointResponse.builder()
                        .date("2026-07-01")
                        .revenue(new BigDecimal("2500000"))
                        .build()))
                .categorySuccess(List.of(CategoryAuctionSuccessResponse.builder()
                        .categoryId(1L)
                        .categoryName("Watches")
                        .successfulAuctions(3)
                        .percentage(100.0)
                        .build()))
                .build();
    }
}
