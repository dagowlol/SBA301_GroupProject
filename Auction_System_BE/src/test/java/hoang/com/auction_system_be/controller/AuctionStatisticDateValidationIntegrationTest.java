package hoang.com.auction_system_be.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class AuctionStatisticDateValidationIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    @WithMockUser(authorities = "ADMIN")
    void getOverview_shouldMapMalformedDateToAnalyticsAppException() throws Exception {
        mockMvc.perform(get("/api/v1/analytics/overview")
                        .param("startDate", "07/01/2026")
                        .param("endDate", "2026-07-31"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(9001))
                .andExpect(jsonPath("$.message").value("Dates must use ISO format yyyy-MM-dd"));
    }
}
