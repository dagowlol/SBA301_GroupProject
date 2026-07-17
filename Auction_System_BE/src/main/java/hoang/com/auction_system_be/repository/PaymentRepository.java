package hoang.com.auction_system_be.repository;

import hoang.com.auction_system_be.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByTransactionId(String transactionId);

    Optional<Payment> findByPaymentGatewayRef(String paymentGatewayRef);

    java.util.Optional<Payment> findByParticipantIdAndType(Long participantId, hoang.com.auction_system_be.enums.PaymentType type);

    java.util.Optional<Payment> findByParticipantUserIdAndParticipantSessionId(Long userId, Long sessionId);
}
