package hoang.com.auction_system_be.service.session.detector;

import hoang.com.auction_system_be.entity.AuctionSession;
import hoang.com.auction_system_be.entity.User;
import hoang.com.auction_system_be.enums.BidStatus;
import hoang.com.auction_system_be.repository.BidRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SuspiciousBidDetector {

    final BidRepository bidRepository;

    @Value("${auction.suspicious.price-jump-multiplier:2.0}")
    double priceJumpMultiplier;

    @Value("${auction.suspicious.spam-limit-window-seconds:10}")
    int spamLimitWindowSeconds;

    @Value("${auction.suspicious.spam-limit-count:5}")
    int spamLimitCount;

    /**
     * Evaluates if a bid is suspicious based on predefined rules.
     *
     * @param session   The auction session
     * @param user      The user placing the bid
     * @param bidAmount The amount of the bid
     * @param now       The timestamp of the bid
     * @return true if suspicious, false otherwise
     */
    public boolean detect(AuctionSession session, User user, BigDecimal bidAmount, LocalDateTime now) {
        // Rule 1: Jump Bidding
        BigDecimal currentPrice = session.getCurrentHighestBid() != null
                ? session.getCurrentHighestBid()
                : session.getItem().getStartingPrice();

        BigDecimal jumpThreshold = currentPrice.multiply(BigDecimal.valueOf(priceJumpMultiplier));
        if (bidAmount.compareTo(jumpThreshold) > 0) {
            return true;
        }

        // Rule 2: Spam Bidding
        LocalDateTime windowStart = now.minusSeconds(spamLimitWindowSeconds);
        long bidCountInWindow = bidRepository.countByParticipantUserIdAndParticipantSessionIdAndBidTimestampGreaterThanEqualAndStatusNot(
                user.getId(), session.getId(), windowStart, BidStatus.CANCELLED);

        if (bidCountInWindow >= spamLimitCount) {
            return true;
        }

        return false;
    }
}
