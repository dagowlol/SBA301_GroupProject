package hoang.com.auction_system_be.service;

import hoang.com.auction_system_be.dto.response.PaymentResponse;
import hoang.com.auction_system_be.entity.Payment;
import hoang.com.auction_system_be.entity.AuctionParticipant;
import hoang.com.auction_system_be.enums.PaymentStatus;
import hoang.com.auction_system_be.enums.PaymentType;
import hoang.com.auction_system_be.exception.AppException;
import hoang.com.auction_system_be.exception.ErrorCode;
import hoang.com.auction_system_be.repository.PaymentRepository;
import hoang.com.auction_system_be.service.impl.CorePaymentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CorePaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private CorePaymentServiceImpl corePaymentService;

    private Payment payment;
    private AuctionParticipant participant;

    @BeforeEach
    void setUp() {
        participant = new AuctionParticipant();
        participant.setId(1L);

        payment = new Payment();
        payment.setId(100L);
        payment.setParticipant(participant);
        payment.setAmount(new BigDecimal("15000000"));
        payment.setStatus(PaymentStatus.PENDING);
        payment.setType(PaymentType.DEPOSIT);
        payment.setTransactionId("100-ABCDE");
    }

    @Test
    void getPaymentStatus_Success() {
        String gatewayRef = "VNP123456";
        payment.setPaymentGatewayRef(gatewayRef);
        payment.setPaymentMethod("VNPAY");

        when(paymentRepository.findByPaymentGatewayRef(gatewayRef)).thenReturn(Optional.of(payment));

        PaymentResponse response = corePaymentService.getPaymentStatus(gatewayRef);

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals(PaymentStatus.PENDING, response.getStatus());
        assertEquals("VNPAY", response.getPaymentMethod());
        assertEquals("VNP123456", response.getPaymentGatewayRef());
        verify(paymentRepository, times(1)).findByPaymentGatewayRef(gatewayRef);
    }

    @Test
    void getPaymentStatus_NotFound_ThrowsException() {
        String gatewayRef = "INVALID";
        when(paymentRepository.findByPaymentGatewayRef(gatewayRef)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class, () -> {
            corePaymentService.getPaymentStatus(gatewayRef);
        });

        assertEquals(ErrorCode.PAYMENT_NOT_FOUND, exception.getErrorCode());
        verify(paymentRepository, times(1)).findByPaymentGatewayRef(gatewayRef);
    }

    @Test
    void updatePaymentStatus_SuccessPaid() {
        Long paymentId = 100L;
        String gatewayRef = "VNP123456";
        String method = "VNPAY";

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

        corePaymentService.updatePaymentStatus(paymentId, gatewayRef, method, true, null);

        assertEquals(PaymentStatus.PAID, payment.getStatus());
        assertEquals(gatewayRef, payment.getPaymentGatewayRef());
        assertEquals(method, payment.getPaymentMethod());
        assertNotNull(payment.getPaidAt());

        verify(paymentRepository, times(1)).save(payment);
    }

    @Test
    void updatePaymentStatus_FailedPayment() {
        Long paymentId = 100L;
        String gatewayRef = "VNP123456";
        String method = "VNPAY";
        String failureReason = "Insufficient funds";

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

        corePaymentService.updatePaymentStatus(paymentId, gatewayRef, method, false, failureReason);

        assertEquals(PaymentStatus.FAILED, payment.getStatus());
        assertEquals(gatewayRef, payment.getPaymentGatewayRef());
        assertEquals(method, payment.getPaymentMethod());
        assertEquals(failureReason, payment.getFailureReason());
        assertNull(payment.getPaidAt());

        verify(paymentRepository, times(1)).save(payment);
    }

    @Test
    void updatePaymentStatus_AlreadyProcessed() {
        Long paymentId = 100L;
        payment.setStatus(PaymentStatus.PAID); // Already processed

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

        corePaymentService.updatePaymentStatus(paymentId, "REF", "VNPAY", true, null);

        // Should just return without saving again
        verify(paymentRepository, never()).save(payment);
    }
}
