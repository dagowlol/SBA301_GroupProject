package hoang.com.auction_system_be.service.feature;
public interface MailService {
    void sendOtpEmail(String toEmail, String otp);
}
