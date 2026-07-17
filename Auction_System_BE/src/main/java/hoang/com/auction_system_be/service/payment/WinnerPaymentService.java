package hoang.com.auction_system_be.service.payment;

public interface WinnerPaymentService {
    void createWinnerPaymentIfAbsent(Long sessionId);
}
