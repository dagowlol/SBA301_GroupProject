package hoang.com.auction_system_be.service.common.otp;

public interface OtpService {
    String generateOtp(String email);

    boolean verifyOtp(String email, String otp);
}
