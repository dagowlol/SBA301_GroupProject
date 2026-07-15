package hoang.com.auction_system_be.service.payment;

import hoang.com.auction_system_be.entity.AuctionParticipant;
import hoang.com.auction_system_be.entity.AuctionSession;
import hoang.com.auction_system_be.entity.Payment;
import hoang.com.auction_system_be.enums.PaymentStatus;
import hoang.com.auction_system_be.enums.PaymentType;
import hoang.com.auction_system_be.repository.AuctionSessionRepository;
import hoang.com.auction_system_be.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class WinnerPaymentServiceImpl implements WinnerPaymentService {

    private final AuctionSessionRepository sessionRepository;
    private final PaymentRepository paymentRepository;

    @Override
    @Transactional
    public void createWinnerPaymentIfAbsent(Long sessionId) {
        AuctionSession session = sessionRepository.findByIdWithDetails(sessionId)
                .orElse(null);

        if (session == null) {
            log.warn("Session with id {} not found when creating winner payment", sessionId);
            return;
        }

        AuctionParticipant winner = session.getCurrentWinnerParticipant();
        if (winner == null) {
            log.info("No winner found for session id {}. Payment creation skipped.", sessionId);
            return;
        }

        // Check if payment already exists
        boolean paymentExists = paymentRepository.findByParticipantIdAndType(winner.getId(), PaymentType.FINAL_PAYMENT).isPresent();
        if (paymentExists) {
            log.info("FINAL_PAYMENT already exists for participant id {} in session {}. Skipping.", winner.getId(), sessionId);
            return;
        }

        // Create Payment
        Payment payment = Payment.builder()
                .participant(winner)
                .amount(session.getCurrentHighestBid())
                .type(PaymentType.FINAL_PAYMENT)
                .status(PaymentStatus.PENDING)
                .build();

        paymentRepository.save(payment);
        log.info("Successfully created FINAL_PAYMENT (PENDING) for winner participant id {} in session {}", winner.getId(), sessionId);
    }
}
