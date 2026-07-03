package hoang.com.auction_system_be.scheduler;

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

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


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

                AtomicInteger updated = new AtomicInteger(0);

                transactionTemplate.executeWithoutResult(status -> {
                    int updatedCount = auctionSessionRepository.updateStatusForIds(SessionStatus.ENDED, sessionIds);
                    updated.set(updatedCount);

                    if (updatedCount == 0) {
                        log.warn("Found {} IDs but updated 0 records to ENDED. Breaking transaction...", sessionIds.size());
                        return;
                    }

                    log.info("Scheduler closed {} active sessions to ENDED. IDs: {}", updatedCount, sessionIds);
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
}