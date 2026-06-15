package hoang.com.auction_system_be.service;

import hoang.com.auction_system_be.dto.response.PaymentResponse;

public interface CorePaymentService {
    PaymentResponse getPaymentStatus(String gatewayRef);
    void updatePaymentStatus(Long paymentId, String gatewayRef, String method, boolean isSuccess, String failureReason);
}
