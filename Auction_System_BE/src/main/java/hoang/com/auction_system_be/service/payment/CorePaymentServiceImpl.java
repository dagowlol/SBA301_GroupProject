package hoang.com.auction_system_be.service.payment;

import hoang.com.auction_system_be.dto.response.PaymentResponse;
import hoang.com.auction_system_be.entity.Payment;
import hoang.com.auction_system_be.entity.AuctionParticipant;
import hoang.com.auction_system_be.enums.PaymentStatus;
import hoang.com.auction_system_be.enums.PaymentType;
import hoang.com.auction_system_be.exception.AppException;
import hoang.com.auction_system_be.exception.ErrorCode;
import hoang.com.auction_system_be.repository.PaymentRepository;
import hoang.com.auction_system_be.repository.AuctionParticipantRepository;
import hoang.com.auction_system_be.service.auth.SecurityContextService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class CorePaymentServiceImpl implements CorePaymentService {

    private final PaymentRepository paymentRepository;
    private final AuctionParticipantRepository participantRepository;
    private final SecurityContextService securityContextService;

    @Override
    public PaymentResponse getPaymentStatus(String gatewayRef) {
        Payment payment = paymentRepository.findByPaymentGatewayRef(gatewayRef)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));

        return PaymentResponse.builder()
                .id(payment.getId())
                .participantId(payment.getParticipant().getId())
                .type(payment.getType())
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .transactionId(payment.getTransactionId())
                .paymentGatewayRef(payment.getPaymentGatewayRef())
                .paymentMethod(payment.getPaymentMethod())
                .paidAt(payment.getPaidAt())
                .failureReason(payment.getFailureReason())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }

    @Override
    @Transactional
    public void updatePaymentStatus(Long paymentId, String gatewayRef, String method, boolean isSuccess,
            String failureReason) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));

        if (payment.getStatus() != PaymentStatus.PENDING) {
            log.warn("Payment ID {} is not in PENDING state. Current state: {}", paymentId, payment.getStatus());
            return; // Already processed
        }

        payment.setPaymentGatewayRef(gatewayRef);
        payment.setPaymentMethod(method);

        if (isSuccess) {
            payment.setStatus(PaymentStatus.PAID);
            payment.setPaidAt(LocalDateTime.now());
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason(failureReason);
        }

        paymentRepository.save(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getMyPaymentForSession(Long sessionId) {
        Long userId = securityContextService.getCurrentUserId();
        if (userId == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        AuctionParticipant participant = participantRepository.findByUserIdAndSessionId(userId, sessionId)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));

        Payment payment = paymentRepository.findByParticipantIdAndType(participant.getId(), PaymentType.FINAL_PAYMENT)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));

        return PaymentResponse.builder()
                .id(payment.getId())
                .participantId(payment.getParticipant().getId())
                .type(payment.getType())
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .transactionId(payment.getTransactionId())
                .paymentGatewayRef(payment.getPaymentGatewayRef())
                .paymentMethod(payment.getPaymentMethod())
                .paidAt(payment.getPaidAt())
                .failureReason(payment.getFailureReason())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }
}
