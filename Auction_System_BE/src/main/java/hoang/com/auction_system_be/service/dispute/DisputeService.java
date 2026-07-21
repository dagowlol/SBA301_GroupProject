package hoang.com.auction_system_be.service.dispute;

import hoang.com.auction_system_be.dto.request.DisputeRequest;
import hoang.com.auction_system_be.dto.request.DisputeResolutionRequest;
import hoang.com.auction_system_be.dto.response.DisputeResponse;
import hoang.com.auction_system_be.dto.response.PageResponse;

public interface DisputeService {
    DisputeResponse createDispute(DisputeRequest request);
    PageResponse<DisputeResponse> getDisputes(int page, int size, Long sessionId, Long raisedById);
    DisputeResponse updateDisputeStatus(Long id, DisputeResolutionRequest request);
}
