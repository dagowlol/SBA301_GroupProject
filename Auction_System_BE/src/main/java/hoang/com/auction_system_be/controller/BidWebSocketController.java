package hoang.com.auction_system_be.controller;

import hoang.com.auction_system_be.dto.request.PlaceBidRequest;
import hoang.com.auction_system_be.service.session.AuctionSessionService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BidWebSocketController {

    AuctionSessionService auctionSessionService;

    @MessageMapping("/auction/{sessionId}/place-bid")
    public void placeBid(
            @DestinationVariable Long sessionId,
            @Payload PlaceBidRequest request) {
        auctionSessionService.placeBid(sessionId, request);
    }
}
