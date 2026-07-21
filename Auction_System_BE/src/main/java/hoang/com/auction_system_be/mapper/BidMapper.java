package hoang.com.auction_system_be.mapper;

import hoang.com.auction_system_be.dto.response.BidLogResponse;
import hoang.com.auction_system_be.dto.response.BidBroadcastResponse;
import hoang.com.auction_system_be.entity.Bid;
import hoang.com.auction_system_be.entity.AuctionParticipant;
import hoang.com.auction_system_be.entity.User;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class BidMapper {

    public BidLogResponse toBidLogResponse(Bid bid) {
        if (bid == null) {
            return null;
        }
        User bidder = bid.getParticipant().getUser();
        String bidderName = bidder != null ? bidder.getFirstName() + " " + bidder.getLastName() : null;

        return BidLogResponse.builder()
                .bidId(bid.getId())
                .sessionId(bid.getParticipant().getSession().getId())
                .userId(bidder != null ? bidder.getId() : null)
                .bidderName(bidderName)
                .amount(bid.getAmount())
                .bidTime(bid.getBidTimestamp())
                .isSuspicious(bid.isSuspicious())
                .status(bid.getStatus() != null ? bid.getStatus().name() : null)
                .build();
    }

    public Bid toBid(AuctionParticipant participant, BigDecimal amount, LocalDateTime bidTimestamp) {
        if (participant == null) {
            return null;
        }
        return Bid.builder()
                .participant(participant)
                .amount(amount)
                .bidTimestamp(bidTimestamp)
                .build();
    }

    public BidBroadcastResponse toBidBroadcastResponse(
            Long sessionId,
            BigDecimal currentPrice,
            String winnerName,
            LocalDateTime endTime,
            LocalDateTime bidTime
    ) {
        return BidBroadcastResponse.builder()
                .sessionId(sessionId)
                .currentPrice(currentPrice)
                .winnerName(winnerName)
                .endTime(endTime)
                .bidTime(bidTime)
                .build();
    }
}
