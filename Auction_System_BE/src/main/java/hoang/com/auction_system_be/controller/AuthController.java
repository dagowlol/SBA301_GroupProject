package hoang.com.auction_system_be.controller;
import hoang.com.auction_system_be.dto.request.*;
import hoang.com.auction_system_be.dto.response.ApiResponse;
import hoang.com.auction_system_be.dto.response.AuthResponse;
import hoang.com.auction_system_be.dto.response.TokenPair;
import hoang.com.auction_system_be.service.system.AuthService;
import hoang.com.auction_system_be.service.system.CookieService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final CookieService cookieService;
    // ─── Register ─────────────────────────────────────────────────────────
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(
            @Valid @RequestBody RegisterRequest request) {
        authService.registerWithOtp(request);
        return ResponseEntity.ok(ApiResponse.success(
                "OTP sent to your email. Please verify to activate your account!"));
    }
    @PostMapping("/register/verify-otp")
    public ResponseEntity<ApiResponse<Void>> verifyRegisterOtp(
            @Valid @RequestBody OtpVerifyRequest request) {
        authService.verifyRegisterOtp(request);
        return ResponseEntity.ok(ApiResponse.success(
                "Account activated successfully! You can now login."));
    }
    // ─── Login ────────────────────────────────────────────────────────────
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Void>> login(
            @Valid @RequestBody LoginRequest request) {
        authService.loginWithOtp(request);
        return ResponseEntity.ok(ApiResponse.success(
                "OTP sent to your email. Please verify to complete login!"));
    }
    @PostMapping("/login/verify-otp")
    public ResponseEntity<ApiResponse<AuthResponse>> verifyLoginOtp(
            @Valid @RequestBody OtpVerifyRequest request,
            HttpServletResponse response) {
        TokenPair tokenPair = authService.verifyOtp(request);
        cookieService.addRefreshTokenCookie(response, tokenPair.getRefreshToken());
        cookieService.addCsrfTokenCookie(response, tokenPair.getCsrfToken());
        AuthResponse authResponse = AuthResponse.builder()
                .accessToken(tokenPair.getAccessToken())
                .build();
        return ResponseEntity.ok(ApiResponse.success(authResponse, "Login successful!"));
    }
    // ─── Resend OTP ───────────────────────────────────────────────────────
    @PostMapping("/resend-otp")
    public ResponseEntity<ApiResponse<Void>> resendOtp(
            @Valid @RequestBody OtpResendRequest request) {
        authService.resendOtp(request.getEmail());
        return ResponseEntity.ok(ApiResponse.success("OTP resent successfully."));
    }
    // ─── Forgot / Reset Password ──────────────────────────────────────────
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Valid @RequestBody PasswordResetRequest request) {
        authService.requestResetPassword(request.getEmail());
        return ResponseEntity.ok(ApiResponse.success(
                "An OTP has been sent to your email to reset your password."));
    }
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody PasswordChangeRequest request) {
        authService.verifyResetPasswordOtp(request);
        return ResponseEntity.ok(ApiResponse.success(
                "Your password has been reset successfully."));
    }
    // ─── Token Management ─────────────────────────────────────────────────
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @CookieValue(name = "refresh_token") String refreshToken,
            @RequestHeader(name = "X-CSRF-TOKEN") String csrfTokenHeader) {
        AuthResponse authResponse = authService.refresh(refreshToken, csrfTokenHeader);
        return ResponseEntity.ok(ApiResponse.success(authResponse, "Token refreshed successfully."));
    }
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            HttpServletResponse response,
            @CookieValue(name = "refresh_token") String refreshToken,
            @RequestHeader(name = "X-CSRF-TOKEN") String csrfTokenHeader) {
        authService.logout(refreshToken, csrfTokenHeader);
        cookieService.clearCookies(response);
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully!"));
    }
}
