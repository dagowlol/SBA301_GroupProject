package hoang.com.auction_system_be.repository;

import hoang.com.auction_system_be.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByTransactionId(String transactionId);

    Optional<Payment> findByPaymentGatewayRef(String paymentGatewayRef);
}
