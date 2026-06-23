package hoang.com.auction_system_be.service.auth;

import hoang.com.auction_system_be.dto.request.*;
import hoang.com.auction_system_be.dto.response.AuthResponse;
import hoang.com.auction_system_be.dto.response.TokenPair;
import hoang.com.auction_system_be.entity.User;
import hoang.com.auction_system_be.enums.RoleName;
import hoang.com.auction_system_be.enums.UserStatus;
import hoang.com.auction_system_be.exception.AppException;
import hoang.com.auction_system_be.exception.ErrorCode;
import hoang.com.auction_system_be.repository.UserRepository;
import hoang.com.auction_system_be.security.UserDetailsImpl;
import hoang.com.auction_system_be.service.common.mail.MailService;
import hoang.com.auction_system_be.service.common.otp.OtpService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;
    private final MailService mailService;
    private final RefreshTokenService refreshTokenService;
    @Value("${jwt.refresh-token.expiration-ms}")
    private long jwtRefreshExpiration;
    @Value("${app.security.lockout.max-attempts:5}")
    private int maxFailedAttempts;
    @Value("${app.security.lockout.duration-minutes:30}")
    private long lockoutDurationMinutes;

    // =========================================================================
    // REGISTER
    // =========================================================================
    @Override
    @Transactional
    public void registerWithOtp(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }
        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                .role(RoleName.USER)
                .status(UserStatus.PENDING)
                .build();
        userRepository.save(user);
        String otp = otpService.generateOtp(request.getEmail());
        mailService.sendOtpEmail(request.getEmail(), otp);
        log.info("User registered (PENDING), OTP sent to: {}", request.getEmail());
    }

    @Override
    @Transactional
    public void verifyRegisterOtp(OtpVerifyRequest request) {
        boolean isValid = otpService.verifyOtp(request.getEmail(), request.getOtp());
        if (!isValid) {
            throw new AppException(ErrorCode.INVALID_OTP);
        }
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
        log.info("User account activated: {}", request.getEmail());
    }

    // =========================================================================
    // LOGIN
    // =========================================================================
    @Override
    @Transactional
    public void loginWithOtp(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        checkAndHandleUserLockout(user);
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()));
            handleSuccessfulLogin(user, authentication);
        } catch (AuthenticationException e) {
            handleFailedLogin(user);
        }
    }

    @Override
    @Transactional
    public TokenPair verifyOtp(OtpVerifyRequest request) {
        boolean isValid = otpService.verifyOtp(request.getEmail(), request.getOtp());
        if (!isValid) {
            throw new AppException(ErrorCode.INVALID_OTP);
        }
        UserDetailsImpl userDetails = userRepository.findByEmail(request.getEmail())
                .map(UserDetailsImpl::build)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        String csrfToken = UUID.randomUUID().toString();
        String accessToken = jwtService.generateToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails, csrfToken);
        refreshTokenService.saveRefreshToken(refreshToken, userDetails.getUsername(), jwtRefreshExpiration);
        log.info("Login verified, tokens issued for: {}", request.getEmail());
        return new TokenPair(accessToken, refreshToken, csrfToken);
    }

    // =========================================================================
    // RESEND OTP
    // =========================================================================
    @Override
    @Transactional
    public void resendOtp(String email) {
        userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        String otp = otpService.generateOtp(email);
        mailService.sendOtpEmail(email, otp);
        log.info("OTP resent to: {}", email);
    }

    // =========================================================================
    // FORGOT / RESET PASSWORD
    // =========================================================================
    @Override
    @Transactional
    public void requestResetPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        switch (user.getStatus()) {
            case LOCKED -> throw new AppException(ErrorCode.USER_LOCKED);
            case INACTIVE -> throw new AppException(ErrorCode.USER_INACTIVE);
            case SUSPENDED -> throw new AppException(ErrorCode.USER_INACTIVE);
            default -> {
            }
        }
        String otp = otpService.generateOtp(email);
        mailService.sendOtpEmail(email, otp);
        log.info("Password reset OTP sent to: {}", email);
    }

    @Override
    @Transactional
    public void verifyResetPasswordOtp(PasswordChangeRequest request) {
        boolean isValid = otpService.verifyOtp(request.getEmail(), request.getOtp());
        if (!isValid) {
            throw new AppException(ErrorCode.INVALID_OTP);
        }
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("Password reset successful for: {}", request.getEmail());
    }

    // =========================================================================
    // TOKEN MANAGEMENT
    // =========================================================================
    @Override
    @Transactional
    public AuthResponse refresh(String refreshToken, String csrfTokenHeader) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new AppException(ErrorCode.REFRESH_TOKEN_IS_MISSING);
        }
        final String userEmail;
        try {
            userEmail = jwtService.extractUsername(refreshToken);
        } catch (Exception e) {
            throw new AppException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
        UserDetails userDetails = userRepository.findByEmail(userEmail)
                .map(UserDetailsImpl::build)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        if (!jwtService.isTokenValid(refreshToken, userDetails)) {
            throw new AppException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
        if (!refreshTokenService.isRefreshTokenValid(refreshToken)) {
            throw new AppException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
        validateCsrfToken(refreshToken, csrfTokenHeader);
        String newAccessToken = jwtService.generateToken(userDetails);
        return AuthResponse.builder().accessToken(newAccessToken).build();
    }

    @Override
    @Transactional
    public void logout(String refreshToken, String csrfTokenHeader) {
        validateCsrfToken(refreshToken, csrfTokenHeader);
        refreshTokenService.revokeToken(refreshToken);
        log.info("User logged out, refresh token revoked.");
    }

    // =========================================================================
    // Private helpers
    // =========================================================================
    private void validateCsrfToken(String refreshToken, String csrfTokenHeader) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new AppException(ErrorCode.REFRESH_TOKEN_IS_MISSING);
        }
        try {
            String csrfFromToken = jwtService.extractCsrfToken(refreshToken);
            if (csrfTokenHeader == null || !csrfTokenHeader.equals(csrfFromToken)) {
                throw new AppException(ErrorCode.INVALID_CSRF_TOKEN);
            }
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            throw new AppException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
    }

    private void checkAndHandleUserLockout(User user) {
        if (user.getStatus() == UserStatus.LOCKED && user.getLockedAt() != null) {
            Instant unlockTime = user.getLockedAt().plus(Duration.ofMinutes(lockoutDurationMinutes));
            if (unlockTime.isBefore(Instant.now())) {
                user.setStatus(UserStatus.ACTIVE);
                user.setFailedLoginAttempts(0);
                user.setLockedAt(null);
                userRepository.save(user);
                log.info("Account auto-unlocked for: {}", user.getEmail());
                return;
            }
        }
        if (user.getStatus() == UserStatus.LOCKED) {
            throw new AppException(ErrorCode.USER_LOCKED);
        }
    }

    private void handleSuccessfulLogin(User user, Authentication authentication) {
        user.setFailedLoginAttempts(0);
        userRepository.save(user);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String email = userDetails.getEmail();
        switch (user.getStatus()) {
            case PENDING -> {
                String otp = otpService.generateOtp(email);
                mailService.sendOtpEmail(email, otp);
                throw new AppException(ErrorCode.USER_NOT_ACTIVE);
            }
            case LOCKED -> throw new AppException(ErrorCode.USER_LOCKED);
            case INACTIVE -> throw new AppException(ErrorCode.USER_INACTIVE);
            case SUSPENDED -> throw new AppException(ErrorCode.USER_INACTIVE);
            case ACTIVE -> {
                String otp = otpService.generateOtp(email);
                mailService.sendOtpEmail(email, otp);
                log.info("Login OTP sent to: {}", email);
            }
            default -> throw new AppException(ErrorCode.USER_STATUS_INVALID);
        }
    }

    private void handleFailedLogin(User user) {
        int newAttempts = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(newAttempts);
        if (newAttempts >= maxFailedAttempts) {
            user.setStatus(UserStatus.LOCKED);
            user.setLockedAt(Instant.now());
            log.warn("Account locked after {} failed attempts: {}", newAttempts, user.getEmail());
        }
        userRepository.save(user);
        throw new AppException(ErrorCode.INVALID_CREDENTIALS);
    }
}
