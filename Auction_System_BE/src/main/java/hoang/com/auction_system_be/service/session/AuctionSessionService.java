package hoang.com.auction_system_be.service.session;

import hoang.com.auction_system_be.dto.request.PlaceBidRequest;
import hoang.com.auction_system_be.dto.response.AuctionSessionDetailResponse;

public interface AuctionSessionService {

    AuctionSessionDetailResponse getAuctionSessionDetail(Long sessionId);

    void placeBid(Long sessionId, PlaceBidRequest request);
}
