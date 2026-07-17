package hoang.com.auction_system_be.mapper;

import hoang.com.auction_system_be.dto.response.AuctionSessionDetailResponse;
import hoang.com.auction_system_be.dto.response.BidLogResponse;
import hoang.com.auction_system_be.dto.response.ErrorSocketResponse;
import hoang.com.auction_system_be.entity.AuctionSession;
import hoang.com.auction_system_be.entity.AuctionParticipant;
import hoang.com.auction_system_be.entity.AuctionExtensionLog;
import hoang.com.auction_system_be.entity.User;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class AuctionSessionMapper {

    public AuctionSessionDetailResponse toDetailResponse(
            AuctionSession session,
            String imageUrl,
            String currentWinnerName,
            List<BidLogResponse> bidLogs) {
        if (session == null) {
            return null;
        }
        Long winnerId = null;
        String winnerName = null;
        if (session.getCurrentWinnerParticipant() != null && session.getCurrentWinnerParticipant().getUser() != null) {
            winnerId = session.getCurrentWinnerParticipant().getUser().getId();
            winnerName = session.getCurrentWinnerParticipant().getUser().getFirstName() + " "
                    + session.getCurrentWinnerParticipant().getUser().getLastName();
        }
        log.info("session status {}", session.getStatus());

        return AuctionSessionDetailResponse.builder()
                .sessionId(session.getId())
                .itemId(session.getItem().getId())
                .itemName(session.getItem().getName())
                .itemImage(imageUrl)
                .itemDescription(session.getItem().getDescription())
                .endTime(session.getEndTime())
                .currentPrice(session.getCurrentHighestBid())
                .currentWinnerName(currentWinnerName)
                .winnerId(winnerId)
                .winnerName(winnerName)
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
            String reason) {
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
