package hoang.com.auction_system_be.service.common.mail;

public interface MailService {
    void sendOtpEmail(String toEmail, String otp);
}
