package hoang.com.auction_system_be.service.impl;

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
import hoang.com.auction_system_be.service.AuctionSessionService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
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

            // Find or create participant
            AuctionParticipant participant = auctionParticipantRepository
                    .findByUserIdAndSessionId(request.getUserId(), sessionId)
                    .orElseGet(() -> {
                        AuctionParticipant newParticipant = auctionSessionMapper.toParticipant(user, session);
                        return auctionParticipantRepository.save(newParticipant);
                    });

            // Save bid
            LocalDateTime now = LocalDateTime.now();
            Bid bid = bidMapper.toBid(participant, request.getBidAmount(), now);
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
                        "Anti-snipe: bid placed within last 30 seconds"
                );
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
                    now
            );

            messagingTemplate.convertAndSend(
                    "/topic/auction/" + sessionId,
                    broadcastResponse
            );

        } catch (AppException e) {
            log.warn("Bid validation failed for session {}: {}", sessionId, e.getMessage());

            // Send private error message to the bidder
            ErrorSocketResponse errorResponse = auctionSessionMapper.toErrorSocketResponse(
                    e.getErrorCode().getMessage()
            );

            messagingTemplate.convertAndSendToUser(
                    request.getUserId().toString(),
                    "/queue/errors",
                    errorResponse
            );
        }
    }
}
