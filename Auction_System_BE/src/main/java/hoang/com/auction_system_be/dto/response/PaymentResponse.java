package hoang.com.auction_system_be.dto.response;

import hoang.com.auction_system_be.enums.PaymentStatus;
import hoang.com.auction_system_be.enums.PaymentType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentResponse {
    Long id;
    Long participantId;
    PaymentType type;
    BigDecimal amount;
    PaymentStatus status;
    String transactionId;
    String paymentGatewayRef;
    String paymentMethod;
    LocalDateTime paidAt;
    String failureReason;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
