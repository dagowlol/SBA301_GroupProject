package hoang.com.auction_system_be.service;

import hoang.com.auction_system_be.config.VNPayConfig;
import hoang.com.auction_system_be.entity.Payment;
import hoang.com.auction_system_be.enums.PaymentStatus;
import hoang.com.auction_system_be.exception.AppException;
import hoang.com.auction_system_be.exception.ErrorCode;
import hoang.com.auction_system_be.repository.PaymentRepository;
import hoang.com.auction_system_be.service.impl.VNPayServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VNPayServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private CorePaymentService corePaymentService;

    @Mock
    private VNPayConfig vnPayConfig;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private VNPayServiceImpl vnPayService;

    private Payment payment;

    @BeforeEach
    void setUp() {
        payment = new Payment();
        payment.setId(10L);
        payment.setAmount(new BigDecimal("15000000"));
        payment.setStatus(PaymentStatus.PENDING);
    }

    @Test
    void createVNPayUrl_Success() {
        when(paymentRepository.findById(10L)).thenReturn(Optional.of(payment));
        when(vnPayConfig.getVnpVersion()).thenReturn("2.1.0");
        when(vnPayConfig.getVnpCommand()).thenReturn("pay");
        when(vnPayConfig.getRandomNumber(5)).thenReturn("12345");
        when(vnPayConfig.getIpAddress(request)).thenReturn("127.0.0.1");
        when(vnPayConfig.getVnpTmnCode()).thenReturn("TMNCODE");
        when(vnPayConfig.getVnpCurrCode()).thenReturn("VND");
        when(vnPayConfig.getVnpReturnUrl()).thenReturn("http://return.url");
        when(vnPayConfig.getVnpUrl()).thenReturn("http://sandbox.vnpayment.vn/paymentv2/vpcpay.html");
        when(vnPayConfig.getVnpHashSecret()).thenReturn("SECRET");
        when(vnPayConfig.hmacSHA512(anyString(), anyString())).thenReturn("hashedString");

        String url = vnPayService.createVNPayUrl(10L, request);

        assertNotNull(url);
        assertTrue(url.startsWith("http://sandbox.vnpayment.vn/paymentv2/vpcpay.html?"));
        assertTrue(url.contains("vnp_Amount=1500000000")); // Amount * 100
        assertTrue(url.contains("vnp_SecureHash=hashedString"));
        verify(paymentRepository, times(1)).save(payment);
        assertEquals("10-12345", payment.getTransactionId());
    }

    @Test
    void createVNPayUrl_PaymentNotFound() {
        when(paymentRepository.findById(10L)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class, () -> vnPayService.createVNPayUrl(10L, request));
        assertEquals(ErrorCode.PAYMENT_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void processVNPayIpn_Success() {
        Map<String, String> params = new HashMap<>();
        params.put("vnp_Amount", "1500000000"); // Matches amount * 100
        params.put("vnp_ResponseCode", "00");
        params.put("vnp_TxnRef", "10-12345");
        params.put("vnp_TransactionNo", "VNP123456");
        params.put("vnp_SecureHash", "correctHash");

        when(vnPayConfig.getVnpHashSecret()).thenReturn("SECRET");
        when(vnPayConfig.hmacSHA512(anyString(), anyString())).thenReturn("correctHash");
        when(paymentRepository.findByTransactionId("10-12345")).thenReturn(Optional.of(payment));

        Map<String, String> result = vnPayService.processVNPayIpn(params);

        assertEquals("00", result.get("RspCode"));
        assertEquals("Confirm Success", result.get("Message"));
        verify(corePaymentService, times(1)).updatePaymentStatus(
                10L, "VNP123456", "VNPAY", true, null
        );
    }

    @Test
    void processVNPayIpn_InvalidChecksum() {
        Map<String, String> params = new HashMap<>();
        params.put("vnp_SecureHash", "wrongHash");

        when(vnPayConfig.getVnpHashSecret()).thenReturn("SECRET");
        when(vnPayConfig.hmacSHA512(anyString(), anyString())).thenReturn("correctHash");

        Map<String, String> result = vnPayService.processVNPayIpn(params);

        assertEquals("97", result.get("RspCode"));
        assertEquals("Invalid Checksum", result.get("Message"));
        verify(corePaymentService, never()).updatePaymentStatus(anyLong(), anyString(), anyString(), anyBoolean(), anyString());
    }

    @Test
    void processVNPayIpn_InvalidAmount() {
        Map<String, String> params = new HashMap<>();
        params.put("vnp_Amount", "1000000000"); // Wrong amount
        params.put("vnp_TxnRef", "10-12345");
        params.put("vnp_SecureHash", "correctHash");

        when(vnPayConfig.getVnpHashSecret()).thenReturn("SECRET");
        when(vnPayConfig.hmacSHA512(anyString(), anyString())).thenReturn("correctHash");
        when(paymentRepository.findByTransactionId("10-12345")).thenReturn(Optional.of(payment));

        Map<String, String> result = vnPayService.processVNPayIpn(params);

        assertEquals("04", result.get("RspCode"));
        assertEquals("Invalid Amount", result.get("Message"));
        verify(corePaymentService, never()).updatePaymentStatus(anyLong(), anyString(), anyString(), anyBoolean(), anyString());
    }
}
