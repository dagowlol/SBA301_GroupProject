package hoang.com.auction_system_be.mapper;

import hoang.com.auction_system_be.dto.response.AuctionSessionDetailResponse;
import hoang.com.auction_system_be.dto.response.BidLogResponse;
import hoang.com.auction_system_be.dto.response.ErrorSocketResponse;
import hoang.com.auction_system_be.entity.AuctionSession;
import hoang.com.auction_system_be.entity.AuctionParticipant;
import hoang.com.auction_system_be.entity.AuctionExtensionLog;
import hoang.com.auction_system_be.entity.User;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class AuctionSessionMapper {

    public AuctionSessionDetailResponse toDetailResponse(
            AuctionSession session,
            String imageUrl,
            String currentWinnerName,
            List<BidLogResponse> bidLogs
    ) {
        if (session == null) {
            return null;
        }
        return AuctionSessionDetailResponse.builder()
                .sessionId(session.getId())
                .itemId(session.getItem().getId())
                .itemName(session.getItem().getName())
                .itemImage(imageUrl)
                .itemDescription(session.getItem().getDescription())
                .endTime(session.getEndTime())
                .currentPrice(session.getCurrentHighestBid())
                .currentWinnerName(currentWinnerName)
                .bidLogs(bidLogs)
                .status(session.getStatus())
                .minimumIncrement(session.getMinimumIncrement())
                .reservePrice(session.getReservePrice())
                .build();
    }

    public AuctionParticipant toParticipant(User user, AuctionSession session) {
        if (user == null || session == null) {
            return null;
        }
        return AuctionParticipant.builder()
                .user(user)
                .session(session)
                .build();
    }

    public AuctionExtensionLog toExtensionLog(
            AuctionSession session,
            AuctionParticipant participant,
            LocalDateTime oldEndTime,
            LocalDateTime newEndTime,
            String reason
    ) {
        if (session == null) {
            return null;
        }
        return AuctionExtensionLog.builder()
                .session(session)
                .triggeredByParticipant(participant)
                .windowSeconds(session.getAntiSnipeWindowSeconds())
                .extensionSeconds(session.getAntiSnipeExtensionSeconds())
                .oldEndTime(oldEndTime)
                .newEndTime(newEndTime)
                .reason(reason)
                .build();
    }

    public ErrorSocketResponse toErrorSocketResponse(String message) {
        return ErrorSocketResponse.builder()
                .message(message)
                .build();
    }
}
