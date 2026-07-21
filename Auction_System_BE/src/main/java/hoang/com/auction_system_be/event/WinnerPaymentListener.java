package hoang.com.auction_system_be.event;

import hoang.com.auction_system_be.service.payment.WinnerPaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class WinnerPaymentListener {

    private final WinnerPaymentService winnerPaymentService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onSessionEnded(SessionEndedEvent event) {
        log.info("Received SessionEndedEvent for session id={}. Processing winner payment...", event.getSessionId());
        try {
            winnerPaymentService.createWinnerPaymentIfAbsent(event.getSessionId());
        } catch (Exception e) {
            log.error("Error creating winner payment for session id={}: {}", event.getSessionId(), e.getMessage(), e);
        }
    }
}
