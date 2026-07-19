package hoang.com.auction_system_be.service.bid;

import hoang.com.auction_system_be.dto.response.BidLogResponse;
import hoang.com.auction_system_be.dto.response.PageResponse;

public interface BidService {
    PageResponse<BidLogResponse> getBidLogs(int page, int size, Long sessionId, Long userId);
    
    void cancelBid(Long bidId);
}
