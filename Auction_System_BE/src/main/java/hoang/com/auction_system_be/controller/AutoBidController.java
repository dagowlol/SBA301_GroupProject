package hoang.com.auction_system_be.controller;

import hoang.com.auction_system_be.dto.request.AutoBidConfigRequest;
import hoang.com.auction_system_be.dto.response.ApiResponse;
import hoang.com.auction_system_be.dto.response.AutoBidConfigResponse;
import hoang.com.auction_system_be.service.auth.SecurityContextService;
import hoang.com.auction_system_be.service.autobid.AutoBidService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auction-sessions")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Auto-Bid Controller", description = "APIs for setting and managing automatic bidding configurations")
public class AutoBidController {

    AutoBidService autoBidService;
    SecurityContextService securityContextService;

    @GetMapping("/{sessionId}/auto-bid")
    @Operation(summary = "Get Auto-Bid configuration", description = "Get the logged in user's active auto-bid configuration for the session.")
    public ApiResponse<AutoBidConfigResponse> getAutoBidConfig(
            @PathVariable Long sessionId) {
        Long userId = securityContextService.getCurrentUserId();
        return ApiResponse.<AutoBidConfigResponse>builder()
                .result(autoBidService.getAutoBidConfig(sessionId, userId))
                .build();
    }

    @PostMapping("/{sessionId}/auto-bid")
    @Operation(summary = "Configure or toggle Auto-Bid", description = "Sets, updates, or toggles the logged in user's automatic bidding setup.")
    public ApiResponse<AutoBidConfigResponse> saveAutoBidConfig(
            @PathVariable Long sessionId,
            @RequestBody AutoBidConfigRequest request) {
        Long userId = securityContextService.getCurrentUserId();
        return ApiResponse.<AutoBidConfigResponse>builder()
                .result(autoBidService.saveAutoBidConfig(sessionId, userId, request))
                .build();
    }
}
