package hoang.com.auction_system_be.service.system;
import hoang.com.auction_system_be.dto.request.*;
import hoang.com.auction_system_be.dto.response.AuthResponse;
import hoang.com.auction_system_be.dto.response.TokenPair;
public interface AuthService {
    void registerWithOtp(RegisterRequest request);
    void verifyRegisterOtp(OtpVerifyRequest request);
    void loginWithOtp(LoginRequest request);
    TokenPair verifyOtp(OtpVerifyRequest request);
    void resendOtp(String email);
    void requestResetPassword(String email);
    void verifyResetPasswordOtp(PasswordChangeRequest request);
    AuthResponse refresh(String refreshToken, String csrfTokenHeader);
    void logout(String refreshToken, String csrfTokenHeader);
    void validateCsrfToken(String refreshToken, String csrfTokenHeader);
}
