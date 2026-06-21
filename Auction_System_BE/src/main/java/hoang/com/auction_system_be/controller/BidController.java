package hoang.com.auction_system_be.controller;

import hoang.com.auction_system_be.dto.response.ApiResponse;
import hoang.com.auction_system_be.dto.response.BidLogResponse;
import hoang.com.auction_system_be.dto.response.PageResponse;
import hoang.com.auction_system_be.service.bid.BidService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/bids")
@Tag(name = "Bid Log API", description = "Admin/Manager API for viewing bid logs")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BidController {

    BidService bidService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'AUCTION_MANAGER')")
    @Operation(summary = "Get bid logs", description = "Get paginated bid logs with optional filters for sessionId and userId. Requires ADMIN or AUCTION_MANAGER role.")
    public ApiResponse<PageResponse<BidLogResponse>> getBidLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long sessionId,
            @RequestParam(required = false) Long userId) {

        return ApiResponse.<PageResponse<BidLogResponse>>builder()
                .result(bidService.getBidLogs(page, size, sessionId, userId))
                .build();
    }
}
