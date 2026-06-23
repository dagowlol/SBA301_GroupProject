package hoang.com.auction_system_be.controller;

import hoang.com.auction_system_be.dto.response.ApiResponse;
import hoang.com.auction_system_be.dto.response.AuctionSessionDetailResponse;
import hoang.com.auction_system_be.service.session.AuctionSessionService;
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
@Tag(name = "Auction Session Controller", description = "APIs for auction session operations")
public class AuctionSessionController {

    AuctionSessionService auctionSessionService;

    @GetMapping("/{id}")
    @Operation(summary = "Get auction session detail", description = "Returns auction session details including item info, current price, winner, and latest 10 bid logs.")
    public ApiResponse<AuctionSessionDetailResponse> getAuctionSessionDetail(
            @PathVariable Long id) {
        return ApiResponse.<AuctionSessionDetailResponse>builder()
                .result(auctionSessionService.getAuctionSessionDetail(id))
                .build();
    }
}
