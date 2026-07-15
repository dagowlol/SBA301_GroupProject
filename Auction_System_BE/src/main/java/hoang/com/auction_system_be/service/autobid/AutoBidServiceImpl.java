package hoang.com.auction_system_be.service.autobid;

import hoang.com.auction_system_be.dto.request.AutoBidConfigRequest;
import hoang.com.auction_system_be.dto.response.AutoBidConfigResponse;
import hoang.com.auction_system_be.dto.response.BidBroadcastResponse;
import hoang.com.auction_system_be.entity.*;
import hoang.com.auction_system_be.enums.SessionStatus;
import hoang.com.auction_system_be.event.AutoBidLimitReachedEvent;
import hoang.com.auction_system_be.event.AutoBidTriggerEvent;
import hoang.com.auction_system_be.event.BidPlacedEvent;
import hoang.com.auction_system_be.event.SessionEndedEvent;
import hoang.com.auction_system_be.exception.AppException;
import hoang.com.auction_system_be.exception.ErrorCode;
import hoang.com.auction_system_be.repository.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AutoBidServiceImpl implements AutoBidService {

    AutoBidConfigRepository autoBidConfigRepository;
    AuctionSessionRepository auctionSessionRepository;
    AuctionParticipantRepository auctionParticipantRepository;
    UserRepository userRepository;
    BidRepository bidRepository;
    AuctionExtensionLogRepository auctionExtensionLogRepository;
    ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(readOnly = true)
    public AutoBidConfigResponse getAutoBidConfig(Long sessionId, Long userId) {
        return autoBidConfigRepository.findByParticipantUserIdAndParticipantSessionId(userId, sessionId)
                .map(this::toResponse)
                .orElseGet(() -> AutoBidConfigResponse.builder()
                        .sessionId(sessionId)
                        .userId(userId)
                        .isActive(false)
                        .build());
    }

    @Override
    @Transactional
    public AutoBidConfigResponse saveAutoBidConfig(Long sessionId, Long userId, AutoBidConfigRequest request) {
        AuctionSession session = auctionSessionRepository.findById(sessionId)
                .orElseThrow(() -> new AppException(ErrorCode.SESSION_NOT_FOUND));

        BigDecimal currentPrice = session.getCurrentHighestBid() != null
                ? session.getCurrentHighestBid()
                : session.getItem().getStartingPrice();

        BigDecimal minIncrement = session.getMinimumIncrement();
        BigDecimal minRequired = currentPrice.add(minIncrement);

        // Validation (AC1)
        if (request.getMaxBidAmount() == null || request.getMaxBidAmount().compareTo(minRequired) < 0) {
            throw new AppException(ErrorCode.INVALID_AUTO_BID_MAX_AMOUNT);
        }

        // Validation for custom increment if provided
        if (request.getBidIncrement() != null && request.getBidIncrement().compareTo(minIncrement) < 0) {
            throw new AppException(ErrorCode.INVALID_AUTO_BID_INCREMENT);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        AuctionParticipant participant = auctionParticipantRepository
                .findByUserIdAndSessionId(userId, sessionId)
                .orElseGet(() -> {
                    AuctionParticipant p = AuctionParticipant.builder()
                            .user(user)
                            .session(session)
                            .joinedAt(LocalDateTime.now())
                            .build();
                    return auctionParticipantRepository.save(p);
                });

        AutoBidConfig config = autoBidConfigRepository
                .findByParticipantUserIdAndParticipantSessionId(userId, sessionId)
                .orElseGet(() -> AutoBidConfig.builder()
                        .participant(participant)
                        .build());

        config.setMaxBidAmount(request.getMaxBidAmount());
        config.setBidIncrement(request.getBidIncrement());
        config.setActive(request.isActive());

        AutoBidConfig saved = autoBidConfigRepository.save(config);

        if (saved.isActive()) {
            eventPublisher.publishEvent(new AutoBidTriggerEvent(this, sessionId));
        }

        return toResponse(saved);
    }

    @Async("autoBidExecutor")
    @EventListener
    @Transactional
    public void onAutoBidTrigger(AutoBidTriggerEvent event) {
        triggerAutoBids(event.getSessionId());
    }

    @Override
    @Transactional
    public void triggerAutoBids(Long sessionId) {
        log.info("Running auto-bidding engine for session {}", sessionId);

        // 1. Fetch session with pessimistic write lock
        AuctionSession session = auctionSessionRepository.findByIdForUpdate(sessionId)
                .orElse(null);
        if (session == null || session.getStatus() != SessionStatus.ACTIVE) {
            return;
        }

        // We run a loop to process competing auto-bids
        while (true) {
            LocalDateTime now = LocalDateTime.now();
            if (now.isAfter(session.getEndTime())) {
                session.setStatus(SessionStatus.ENDED);
                auctionSessionRepository.save(session);
                eventPublisher.publishEvent(new SessionEndedEvent(this, session.getId()));
                break;
            }

            AuctionParticipant currentWinner = session.getCurrentWinnerParticipant();
            Long currentWinnerUserId = (currentWinner != null) ? currentWinner.getUser().getId() : null;

            // Find all active auto-bids for this session
            List<AutoBidConfig> activeConfigs = autoBidConfigRepository.findByParticipantSessionIdAndIsActiveTrue(sessionId);

            // Filter out the current winner
            List<AutoBidConfig> otherConfigs = activeConfigs.stream()
                    .filter(c -> !c.getParticipant().getUser().getId().equals(currentWinnerUserId))
                    .toList();

            if (otherConfigs.isEmpty()) {
                break; // No other active robots to bid
            }

            // Select the next bidder. Sort by config ID to be deterministic and fair
            AutoBidConfig nextConfig = otherConfigs.stream()
                    .min((c1, c2) -> c1.getId().compareTo(c2.getId()))
                    .orElse(null);

            if (nextConfig == null) {
                break;
            }

            BigDecimal currentPrice = session.getCurrentHighestBid() != null
                    ? session.getCurrentHighestBid()
                    : session.getItem().getStartingPrice();

            BigDecimal minIncrement = session.getMinimumIncrement();
            BigDecimal robotIncrement = nextConfig.getBidIncrement();
            BigDecimal increment = (robotIncrement != null && robotIncrement.compareTo(minIncrement) >= 0)
                    ? robotIncrement
                    : minIncrement;

            BigDecimal nextBidAmount = currentPrice.add(increment);

            // Check if user has exceeded their max limit
            if (nextBidAmount.compareTo(nextConfig.getMaxBidAmount()) > 0) {
                // Limit exceeded! Deactivate bot
                nextConfig.setActive(false);
                autoBidConfigRepository.save(nextConfig);

                log.info("User {} auto-bid deactivated: limit {} exceeded by bid {}",
                        nextConfig.getParticipant().getUser().getId(), nextConfig.getMaxBidAmount(), nextBidAmount);

                // Publish limit reached event
                eventPublisher.publishEvent(new AutoBidLimitReachedEvent(
                        this,
                        nextConfig.getParticipant().getUser().getId(),
                        sessionId,
                        nextConfig.getMaxBidAmount()
                ));
                continue; // check other configurations
            }

            // Place the bid!
            Bid bid = Bid.builder()
                    .participant(nextConfig.getParticipant())
                    .amount(nextBidAmount)
                    .isAutoBid(true)
                    .bidTimestamp(now)
                    .build();
            bidRepository.save(bid);

            // Update session state
            session.setCurrentHighestBid(nextBidAmount);
            session.setCurrentWinnerParticipant(nextConfig.getParticipant());
            session.setBidCount(session.getBidCount() + 1);

            // Handle anti-snipe
            LocalDateTime oldEndTime = session.getEndTime();
            long secondsRemaining = Duration.between(now, oldEndTime).getSeconds();
            if (secondsRemaining <= session.getAntiSnipeWindowSeconds()) {
                LocalDateTime newEndTime = oldEndTime.plusSeconds(session.getAntiSnipeExtensionSeconds());
                session.setEndTime(newEndTime);

                AuctionExtensionLog extensionLog = AuctionExtensionLog.builder()
                        .session(session)
                        .oldEndTime(oldEndTime)
                        .newEndTime(newEndTime)
                        .triggeredByParticipant(nextConfig.getParticipant())
                        .windowSeconds(session.getAntiSnipeWindowSeconds())
                        .extensionSeconds(session.getAntiSnipeExtensionSeconds())
                        .reason("Anti-snipe: auto bid placed within window")
                        .build();
                auctionExtensionLogRepository.save(extensionLog);

                log.info("Anti-snipe triggered: extended session {} from {} to {}",
                        sessionId, oldEndTime, newEndTime);
            }

            auctionSessionRepository.save(session);

            log.info("Placed auto-bid for user {} at amount {}",
                    nextConfig.getParticipant().getUser().getId(), nextBidAmount);

            // Broadcast the new bid
            String winnerName = nextConfig.getParticipant().getUser().getFirstName() + " " + nextConfig.getParticipant().getUser().getLastName();
            BidBroadcastResponse broadcastResponse = BidBroadcastResponse.builder()
                    .sessionId(session.getId())
                    .currentPrice(nextBidAmount)
                    .winnerName(winnerName)
                    .endTime(session.getEndTime())
                    .bidTime(now)
                    .build();

            eventPublisher.publishEvent(new BidPlacedEvent(this, String.valueOf(session.getId()), broadcastResponse));
        }
    }

    private AutoBidConfigResponse toResponse(AutoBidConfig config) {
        if (config == null) return null;
        return AutoBidConfigResponse.builder()
                .id(config.getId())
                .sessionId(config.getParticipant().getSession().getId())
                .userId(config.getParticipant().getUser().getId())
                .maxBidAmount(config.getMaxBidAmount())
                .bidIncrement(config.getBidIncrement())
                .isActive(config.isActive())
                .build();
    }
}
