package hoang.com.auction_system_be.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;


@Component
@Slf4j
public class AuctionSessionEventListener {

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onSessionCreated(AuctionSessionCreatedEvent event) {
        Long sessionId = event.getSession().getId();
        Long staffId   = event.getCreatedByUserId();

        log.info("[EVENT] AuctionSessionCreated – sessionId={}, createdBy={}, startTime={}, endTime={}",
                sessionId,
                staffId,
                event.getSession().getStartTime(),
                event.getSession().getEndTime());

        // notificationService.notifySessionScheduled(event.getSession());

        // quartzService.scheduleActivateJob(event.getSession());
    }
}
