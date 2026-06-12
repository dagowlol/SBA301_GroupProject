package hoang.com.auction_system_be.entity;

import hoang.com.auction_system_be.enums.PaymentStatus;
import hoang.com.auction_system_be.enums.PaymentType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments", indexes = {
        @Index(name = "idx_payment_participant", columnList = "participant_id"),
        @Index(name = "idx_payment_type", columnList = "type"),
        @Index(name = "idx_payment_status", columnList = "status"),
        @Index(name = "idx_payment_tx", columnList = "transaction_id", unique = true)
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "participant_id", nullable = false)
    AuctionParticipant participant;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    PaymentType type = PaymentType.FINAL_PAYMENT;

    @Column(nullable = false, precision = 18, scale = 2)
    BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    PaymentStatus status = PaymentStatus.PENDING;

    @Column(name = "transaction_id", unique = true, length = 255)
    String transactionId;

    @Column(name = "payment_gateway_ref", length = 255)
    String paymentGatewayRef;

    @Column(name = "payment_method", length = 50)
    String paymentMethod;

    @Column(name = "paid_at")
    LocalDateTime paidAt;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    String failureReason;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    LocalDateTime updatedAt;
}

