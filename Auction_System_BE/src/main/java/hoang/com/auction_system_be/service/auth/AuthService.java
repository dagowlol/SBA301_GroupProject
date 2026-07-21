package hoang.com.auction_system_be.service.auth;

import hoang.com.auction_system_be.dto.request.*;
import hoang.com.auction_system_be.dto.response.AuthResponse;
import hoang.com.auction_system_be.dto.response.TokenPair;
import hoang.com.auction_system_be.dto.response.UserResponse;

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

    UserResponse getCurrentUser(String email);

}
