package hoang.com.auction_system_be.controller;

import hoang.com.auction_system_be.dto.request.*;
import hoang.com.auction_system_be.dto.response.ApiResponse;
import hoang.com.auction_system_be.dto.response.AuthResponse;
import hoang.com.auction_system_be.dto.response.TokenPair;
import hoang.com.auction_system_be.dto.response.UserResponse;
import hoang.com.auction_system_be.service.auth.AuthService;
import hoang.com.auction_system_be.service.auth.CookieService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import hoang.com.auction_system_be.exception.AppException;
import hoang.com.auction_system_be.exception.ErrorCode;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for user authentication, registration and token management")
public class AuthController {
    private final AuthService authService;
    private final CookieService cookieService;

    // ─── Register ─────────────────────────────────────────────────────────

    @Operation(summary = "Register a new user", description = "Sends an OTP to the user's email for registration")
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(
            @Valid @RequestBody RegisterRequest request) {
        checkAlreadyAuthenticated();
        authService.registerWithOtp(request);
        return ResponseEntity.ok(ApiResponse.success(
                "OTP sent to your email. Please verify to activate your account!"));
    }

    @Operation(summary = "Verify registration OTP", description = "Verifies the OTP sent to email and activates the user account")
    @PostMapping("/register/verify-otp")
    public ResponseEntity<ApiResponse<Void>> verifyRegisterOtp(
            @Valid @RequestBody OtpVerifyRequest request) {
        authService.verifyRegisterOtp(request);
        return ResponseEntity.ok(ApiResponse.success(
                "Account activated successfully! You can now login."));
    }

    // ─── Login ────────────────────────────────────────────────────────────

    @Operation(summary = "Login with OTP", description = "Sends an OTP to the user's email for login")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Void>> login(
            @Valid @RequestBody LoginRequest request) {
        checkAlreadyAuthenticated();
        authService.loginWithOtp(request);
        return ResponseEntity.ok(ApiResponse.success(
                "OTP sent to your email. Please verify to complete login!"));
    }

    @Operation(summary = "Verify login OTP", description = "Verifies the OTP and returns access token, refresh token (cookie), and CSRF token (cookie)")
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

    @Operation(summary = "Resend OTP", description = "Resends an OTP to the user's email")
    @PostMapping("/resend-otp")
    public ResponseEntity<ApiResponse<Void>> resendOtp(
            @Valid @RequestBody OtpResendRequest request) {
        authService.resendOtp(request.getEmail());
        return ResponseEntity.ok(ApiResponse.success("OTP resent successfully."));
    }

    // ─── Forgot / Reset Password ──────────────────────────────────────────

    @Operation(summary = "Forgot password", description = "Sends an OTP to the user's email to reset password")
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Valid @RequestBody PasswordResetRequest request) {
        checkAlreadyAuthenticated();
        authService.requestResetPassword(request.getEmail());
        return ResponseEntity.ok(ApiResponse.success(
                "An OTP has been sent to your email to reset your password."));
    }

    @Operation(summary = "Reset password", description = "Verifies OTP and resets the user's password")
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody PasswordChangeRequest request) {
        authService.verifyResetPasswordOtp(request);
        return ResponseEntity.ok(ApiResponse.success(
                "Your password has been reset successfully."));
    }

    // ─── Token Management ─────────────────────────────────────────────────

    @Operation(summary = "Refresh token", description = "Refreshes the access token using the refresh token from cookie")
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @CookieValue(name = "refresh_token") String refreshToken,
            @RequestHeader(name = "X-CSRF-TOKEN") String csrfTokenHeader) {
        AuthResponse authResponse = authService.refresh(refreshToken, csrfTokenHeader);
        return ResponseEntity.ok(ApiResponse.success(authResponse, "Token refreshed successfully."));
    }

    @Operation(summary = "Logout", description = "Logs out the user and clears tokens and cookies")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            HttpServletResponse response,
            @CookieValue(name = "refresh_token") String refreshToken,
            @RequestHeader(name = "X-CSRF-TOKEN") String csrfTokenHeader) {
        authService.logout(refreshToken, csrfTokenHeader);
        cookieService.clearCookies(response);
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully!"));
    }

    // ─── Current User ───────────────────────────────────────────────────

    @Operation(summary = "Get current user", description = "Returns the authenticated user's data from the database in real time")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        String email = auth.getName();
        UserResponse userResponse = authService.getCurrentUser(email);
        return ResponseEntity.ok(ApiResponse.success(userResponse));
    }

    private void checkAlreadyAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            throw new AppException(ErrorCode.ALREADY_AUTHENTICATED);
        }
    }
}
