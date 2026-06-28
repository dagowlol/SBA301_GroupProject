package hoang.com.auction_system_be.service.session;

import hoang.com.auction_system_be.dto.request.AuctionSessionRequest;
import hoang.com.auction_system_be.dto.request.AuctionSessionUpdateRequest;
import hoang.com.auction_system_be.dto.request.PlaceBidRequest;
import hoang.com.auction_system_be.dto.response.AuctionSessionDetailResponse;
import hoang.com.auction_system_be.dto.response.AuctionSessionListResponse;
import hoang.com.auction_system_be.dto.response.AuctionSessionResponse;
import hoang.com.auction_system_be.dto.response.CursorPageResponse;
import hoang.com.auction_system_be.enums.SessionStatus;

public interface AuctionSessionService {

    AuctionSessionDetailResponse getAuctionSessionDetail(Long sessionId);

    void placeBid(Long sessionId, PlaceBidRequest request);

    // ─── Staff/Admin CRUD ─────────────────────────────────────────────────

    AuctionSessionResponse createSession(AuctionSessionRequest request);

    CursorPageResponse<AuctionSessionListResponse> getSessions(Long cursor, int size, String search,
            SessionStatus status);

    AuctionSessionResponse getSessionById(Long id);

    AuctionSessionResponse updateSession(Long id, AuctionSessionUpdateRequest request);

    void deleteSession(Long id);
}
