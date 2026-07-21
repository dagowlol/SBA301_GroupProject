package hoang.com.auction_system_be.service.payment;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

public interface VNPayService {
    String createVNPayUrl(Long paymentId, HttpServletRequest request);

    Map<String, String> processVNPayIpn(Map<String, String> params);
}
