package hoang.com.auction_system_be.service.session;

import hoang.com.auction_system_be.dto.request.PlaceBidRequest;
import hoang.com.auction_system_be.dto.response.AuctionSessionDetailResponse;
import hoang.com.auction_system_be.dto.response.BidBroadcastResponse;
import hoang.com.auction_system_be.dto.response.BidLogResponse;
import hoang.com.auction_system_be.dto.response.ErrorSocketResponse;
import hoang.com.auction_system_be.entity.*;
import hoang.com.auction_system_be.enums.SessionStatus;
import hoang.com.auction_system_be.exception.AppException;
import hoang.com.auction_system_be.exception.ErrorCode;
import hoang.com.auction_system_be.mapper.AuctionSessionMapper;
import hoang.com.auction_system_be.mapper.BidMapper;
import hoang.com.auction_system_be.repository.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AuctionSessionServiceImpl implements AuctionSessionService {

        AuctionSessionRepository auctionSessionRepository;
        BidRepository bidRepository;
        UserRepository userRepository;
        AuctionParticipantRepository auctionParticipantRepository;
        AuctionExtensionLogRepository auctionExtensionLogRepository;
        SimpMessagingTemplate messagingTemplate;
        AuctionSessionMapper auctionSessionMapper;
        BidMapper bidMapper;

        @NonFinal
        @Value("${auction.anti-shill.window-seconds:5}")
        int antiShillWindowSeconds;

        @NonFinal
        @Value("${auction.anti-shill.max-bids:4}")
        int antiShillMaxBids;

        @NonFinal
        @Value("${auction.anti-spam.cooldown-millis:500}")
        long antiSpamCooldownMillis;

        @Override
        @Transactional(readOnly = true)
        public AuctionSessionDetailResponse getAuctionSessionDetail(Long sessionId) {
                AuctionSession session = auctionSessionRepository.findById(sessionId)
                                .orElseThrow(() -> new AppException(ErrorCode.SESSION_NOT_FOUND));

                AuctionItem item = session.getItem();

                // Get the primary image URL, fallback to first image if no primary
                String imageUrl = item.getImages().stream()
                                .filter(ItemImage::isPrimary)
                                .findFirst()
                                .or(() -> item.getImages().stream().findFirst())
                                .map(ItemImage::getImageUrl)
                                .orElse(null);

                // Get current winner name
                String currentWinnerName = null;
                if (session.getCurrentWinnerParticipant() != null) {
                        User winner = session.getCurrentWinnerParticipant().getUser();
                        currentWinnerName = winner.getFirstName() + " " + winner.getLastName();
                }

                // Get latest 10 bid logs
                List<Bid> recentBids = bidRepository
                                .findTop10ByParticipantSessionIdOrderByBidTimestampDesc(sessionId);

                List<BidLogResponse> bidLogs = recentBids.stream()
                                .map(bidMapper::toBidLogResponse)
                                .collect(Collectors.toList());

                return auctionSessionMapper.toDetailResponse(session, imageUrl, currentWinnerName, bidLogs);
        }

        @Override
        @Transactional
        public void placeBid(Long sessionId, PlaceBidRequest request) {
                try {
                        // Rule 1: Auction session must exist
                        AuctionSession session = auctionSessionRepository.findById(sessionId)
                                        .orElseThrow(() -> new AppException(ErrorCode.SESSION_NOT_FOUND));

                        // Rule 2: Session must be ACTIVE
                        if (session.getStatus() != SessionStatus.ACTIVE) {
                                throw new AppException(ErrorCode.SESSION_NOT_ACTIVE);
                        }
                        if (LocalDateTime.now().isAfter(session.getEndTime())) {

                                session.setStatus(SessionStatus.ENDED);
                                auctionSessionRepository.save(session);

                                throw new AppException(ErrorCode.AUCTION_ENDED);
                        }
                        // Rule 3 & 4: Bid amount validation
                        BigDecimal currentPrice = session.getCurrentHighestBid() != null
                                        ? session.getCurrentHighestBid()
                                        : session.getItem().getStartingPrice();

                        BigDecimal minimumBid = currentPrice.add(session.getMinimumIncrement());

                        if (request.getBidAmount().compareTo(minimumBid) < 0) {
                                throw new AppException(ErrorCode.INVALID_BID_AMOUNT);
                        }

                        // Find the user
                        User user = userRepository.findById(request.getUserId())
                                        .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

                        // Anti-spam check: prevent multiple bids within configured cooldown
                        LocalDateTime now = LocalDateTime.now();
                        validateSpamCooldown(sessionId, request.getUserId(), now);

                        // Find or create participant
                        AuctionParticipant participant = auctionParticipantRepository
                                        .findByUserIdAndSessionId(request.getUserId(), sessionId)
                                        .orElseGet(() -> {
                                                AuctionParticipant newParticipant = auctionSessionMapper
                                                                .toParticipant(user, session);
                                                return auctionParticipantRepository.save(newParticipant);
                                        });

                        // Anti-shill bidding check: 2 users bidding excessively
                        boolean isSuspicious = isShillBiddingDetected(sessionId, request.getUserId(), now);

                        // Save bid
                        Bid bid = bidMapper.toBid(participant, request.getBidAmount(), now);
                        bid.setSuspicious(isSuspicious);
                        bidRepository.save(bid);

                        // Update session
                        session.setCurrentHighestBid(request.getBidAmount());
                        session.setCurrentWinnerParticipant(participant);
                        session.setBidCount(session.getBidCount() + 1);

                        // Anti-snipe: extend if <= 30 seconds remaining
                        LocalDateTime oldEndTime = session.getEndTime();
                        long secondsRemaining = Duration.between(now, oldEndTime).getSeconds();

                        if (secondsRemaining <= 30) {
                                LocalDateTime newEndTime = oldEndTime.plusSeconds(30);
                                session.setEndTime(newEndTime);

                                AuctionExtensionLog extensionLog = auctionSessionMapper.toExtensionLog(
                                                session,
                                                participant,
                                                oldEndTime,
                                                newEndTime,
                                                "Anti-snipe: bid placed within last 30 seconds");
                                auctionExtensionLogRepository.save(extensionLog);

                                log.info("Anti-snipe triggered for session {}: extended from {} to {}",
                                                sessionId, oldEndTime, newEndTime);
                        }

                        auctionSessionRepository.save(session);

                        log.info("Bid placed successfully - session: {}, user: {}, amount: {}",
                                        sessionId, request.getUserId(), request.getBidAmount());

                        // Broadcast to all subscribers
                        String winnerName = user.getFirstName() + " " + user.getLastName();
                        BidBroadcastResponse broadcastResponse = bidMapper.toBidBroadcastResponse(
                                        sessionId,
                                        request.getBidAmount(),
                                        winnerName,
                                        session.getEndTime(),
                                        now);

                        messagingTemplate.convertAndSend(
                                        "/topic/auction/" + sessionId,
                                        broadcastResponse);

                } catch (AppException e) {
                        log.warn("Bid validation failed for session {}: {}", sessionId, e.getMessage());

                        // Send private error message to the bidder
                        ErrorSocketResponse errorResponse = auctionSessionMapper.toErrorSocketResponse(
                                        e.getErrorCode().getMessage());

                        messagingTemplate.convertAndSendToUser(
                                        request.getUserId().toString(),
                                        "/queue/errors",
                                        errorResponse);
                }
        }

        private boolean isShillBiddingDetected(Long sessionId, Long currentUserId, LocalDateTime now) {
                List<Bid> recentBids = bidRepository.findTop10ByParticipantSessionIdOrderByBidTimestampDesc(sessionId);
                long recentBidsCount = recentBids.stream()
                        .filter(b -> Duration.between(b.getBidTimestamp(), now).getSeconds() <= antiShillWindowSeconds)
                        .count();

                if (recentBidsCount >= antiShillMaxBids) {
                        java.util.Set<Long> userIds = recentBids.stream()
                                .filter(b -> Duration.between(b.getBidTimestamp(), now).getSeconds() <= antiShillWindowSeconds)
                                .map(b -> b.getParticipant().getUser().getId())
                                .collect(Collectors.toSet());
                        userIds.add(currentUserId);
                        
                        if (userIds.size() == 2) {
                                log.warn("Shill bidding detected for session {}: 2 users placed >={} bids in {}s", 
                                        sessionId, antiShillMaxBids, antiShillWindowSeconds);
                                return true;
                        }
                }
                return false;
        }

        private void validateSpamCooldown(Long sessionId, Long userId, LocalDateTime now) {
                bidRepository.findTopByParticipantUserIdAndParticipantSessionIdOrderByBidTimestampDesc(userId, sessionId)
                        .ifPresent(lastBid -> {
                                long millisBetween = Duration.between(lastBid.getBidTimestamp(), now).toMillis();
                                if (millisBetween < antiSpamCooldownMillis) {
                                        throw new AppException(ErrorCode.TOO_MANY_REQUESTS);
                                }
                        });
        }
}
