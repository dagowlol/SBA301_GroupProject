package hoang.com.auction_system_be.scheduler;

import hoang.com.auction_system_be.entity.AuctionSession;
import hoang.com.auction_system_be.enums.SessionStatus;
import hoang.com.auction_system_be.event.SessionEndedEvent;
import hoang.com.auction_system_be.event.SessionStartedEvent;
import hoang.com.auction_system_be.repository.AuctionSessionRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AuctionSessionScheduler {

    AuctionSessionRepository auctionSessionRepository;
    ApplicationEventPublisher eventPublisher;
    TransactionTemplate transactionTemplate;

    
    @Scheduled(fixedDelay = 10000)
    public void activateScheduledSessions() {
        while (true) {
            try {
                LocalDateTime now = LocalDateTime.now();
                Page<Long> page = auctionSessionRepository.findIdsByStatusAndStartTimeBefore(
                        SessionStatus.SCHEDULED, now, PageRequest.of(0, 100));

                List<Long> sessionIds = page.getContent();
                if (sessionIds.isEmpty()) {
                    break; 
                }

                AtomicInteger updated = new AtomicInteger(0);

                transactionTemplate.executeWithoutResult(status -> {
                    int updatedCount = auctionSessionRepository.updateStatusForIds(SessionStatus.ACTIVE, sessionIds);
                    updated.set(updatedCount);
                    
                    if (updatedCount == 0) {
                        log.warn("Found {} IDs but updated 0 records to ACTIVE. Breaking transaction to prevent infinite loop.", sessionIds.size());
                        return;
                    }

                    log.info("Scheduler opened {} scheduled sessions to ACTIVE. IDs: {}", updatedCount, sessionIds);
                    sessionIds.forEach(id -> eventPublisher.publishEvent(new SessionStartedEvent(this, id)));
                });
                
                if (updated.get() == 0) {
                    break;
                }
                
            } catch (Exception e) {
                log.error("Error occurred while activating scheduled sessions: {}", e.getMessage(), e);
                break;
            }
        }
    }

    
    @Scheduled(fixedDelay = 10000)
    public void closeExpiredSessions() {
        while (true) {
            try {
                LocalDateTime now = LocalDateTime.now();
                Page<Long> page = auctionSessionRepository.findIdsByStatusAndEndTimeBefore(
                        SessionStatus.ACTIVE, now, PageRequest.of(0, 100));

                List<Long> sessionIds = page.getContent();
                if (sessionIds.isEmpty()) {
                    break;
                }

                // Fetch sessions to check reserve price
                List<AuctionSession> sessions =
                        auctionSessionRepository.findAllById(sessionIds);

                // Determine final status per session
                Map<Long, SessionStatus> statusMap = sessions.stream()
                        .collect(Collectors.toMap(
                                AuctionSession::getId,
                                s -> isReserveMet(s) ? SessionStatus.ENDED : SessionStatus.RESERVE_NOT_MET
                        ));

                List<Long> endedIds = new ArrayList<>();
                List<Long> reserveNotMetIds = new ArrayList<>();
                statusMap.forEach((id, status) -> {
                    if (status == SessionStatus.ENDED) endedIds.add(id);
                    else reserveNotMetIds.add(id);
                });

                AtomicInteger updated = new AtomicInteger(0);

                transactionTemplate.executeWithoutResult(status -> {
                    if (!endedIds.isEmpty()) {
                        updated.addAndGet(auctionSessionRepository.updateStatusForIds(SessionStatus.ENDED, endedIds));
                    }
                    if (!reserveNotMetIds.isEmpty()) {
                        updated.addAndGet(auctionSessionRepository.updateStatusForIds(SessionStatus.RESERVE_NOT_MET, reserveNotMetIds));
                    }

                    if (updated.get() == 0) {
                        log.warn("Found {} IDs but updated 0 records. Breaking transaction...", sessionIds.size());
                        return;
                    }

                    log.info("Scheduler closed {} active sessions. Ended: {}, Reserve not met: {}",
                            updated.get(), endedIds.size(), reserveNotMetIds.size());
                    sessionIds.forEach(id -> eventPublisher.publishEvent(new SessionEndedEvent(this, id)));
                });

                if (updated.get() == 0) {
                    break;
                }

            } catch (Exception e) {
                log.error("Error occurred while closing expired sessions: {}", e.getMessage(), e);
                break;
            }
        }
    }

    private boolean isReserveMet(AuctionSession session) {
        BigDecimal highestBid = session.getCurrentHighestBid();
        BigDecimal reservePrice = session.getReservePrice();

        // No bids placed → reserve not met
        if (highestBid == null) {
            return false;
        }
        // No reserve price set → any bid is sufficient
        if (reservePrice == null) {
            return true;
        }
        return highestBid.compareTo(reservePrice) >= 0;
    }
}
