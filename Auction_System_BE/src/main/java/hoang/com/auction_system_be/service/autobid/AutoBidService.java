package hoang.com.auction_system_be.service.autobid;

import hoang.com.auction_system_be.dto.request.AutoBidConfigRequest;
import hoang.com.auction_system_be.dto.response.AutoBidConfigResponse;

public interface AutoBidService {
    AutoBidConfigResponse getAutoBidConfig(Long sessionId, Long userId);
    AutoBidConfigResponse saveAutoBidConfig(Long sessionId, Long userId, AutoBidConfigRequest request);
    void triggerAutoBids(Long sessionId);
}
