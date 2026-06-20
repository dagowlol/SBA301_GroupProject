package hoang.com.auction_system_be.service.impl.system;

import hoang.com.auction_system_be.service.system.*;

import hoang.com.auction_system_be.dto.request.LoginRequest;
import hoang.com.auction_system_be.dto.request.OtpVerifyRequest;
import hoang.com.auction_system_be.dto.request.RegisterRequest;
import hoang.com.auction_system_be.dto.response.AuthResponse;
import hoang.com.auction_system_be.dto.response.TokenPair;
import hoang.com.auction_system_be.entity.User;
import hoang.com.auction_system_be.enums.RoleName;
import hoang.com.auction_system_be.enums.UserStatus;
import hoang.com.auction_system_be.exception.AppException;
import hoang.com.auction_system_be.exception.ErrorCode;
import hoang.com.auction_system_be.repository.UserRepository;
import hoang.com.auction_system_be.security.UserDetailsImpl;
import hoang.com.auction_system_be.service.feature.MailService;
import hoang.com.auction_system_be.service.feature.OtpService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtService jwtService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private OtpService otpService;
    @Mock
    private MailService mailService;
    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthServiceImpl authService;

    private User user;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "jwtRefreshExpiration", 86400000L);
        ReflectionTestUtils.setField(authService, "maxFailedAttempts", 5);
        ReflectionTestUtils.setField(authService, "lockoutDurationMinutes", 30L);

        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setPasswordHash("hashedPassword");
        user.setStatus(UserStatus.ACTIVE);
        user.setRole(RoleName.USER);
        user.setFailedLoginAttempts(0);
    }

    // ================= REGISTER =================

    @Test
    void registerWithOtp_Success() {
        RegisterRequest req = new RegisterRequest();
        req.setFirstName("First");
        req.setLastName("Last");
        req.setEmail("test@example.com");
        req.setPassword("pass123");
        req.setPhoneNumber("0123456789");
        when(userRepository.existsByEmail(req.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(req.getPassword())).thenReturn("hashed");
        when(otpService.generateOtp(req.getEmail())).thenReturn("123456");

        authService.registerWithOtp(req);

        verify(userRepository).save(any(User.class));
        verify(mailService).sendOtpEmail(req.getEmail(), "123456");
    }

    @Test
    void registerWithOtp_ThrowsUserExisted() {
        RegisterRequest req = new RegisterRequest();
        req.setFirstName("First");
        req.setLastName("Last");
        req.setEmail("test@example.com");
        req.setPassword("pass");
        req.setPhoneNumber("123");
        when(userRepository.existsByEmail(req.getEmail())).thenReturn(true);

        AppException ex = assertThrows(AppException.class, () -> authService.registerWithOtp(req));
        assertEquals(ErrorCode.USER_EXISTED, ex.getErrorCode());
    }

    @Test
    void verifyRegisterOtp_Success() {
        OtpVerifyRequest req = new OtpVerifyRequest();
        req.setEmail("test@example.com");
        req.setOtp("123456");
        when(otpService.verifyOtp(req.getEmail(), req.getOtp())).thenReturn(true);
        when(userRepository.findByEmail(req.getEmail())).thenReturn(Optional.of(user));

        user.setStatus(UserStatus.PENDING);
        authService.verifyRegisterOtp(req);

        assertEquals(UserStatus.ACTIVE, user.getStatus());
        verify(userRepository).save(user);
    }

    // ================= LOGIN =================

    @Test
    void loginWithOtp_Success_SendsOtp() {
        LoginRequest req = new LoginRequest();
        req.setEmail("test@example.com");
        req.setPassword("password");
        when(userRepository.findByEmail(req.getEmail())).thenReturn(Optional.of(user));

        Authentication authentication = mock(Authentication.class);
        UserDetailsImpl userDetails = mock(UserDetailsImpl.class);
        when(userDetails.getEmail()).thenReturn(req.getEmail());
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        when(otpService.generateOtp(req.getEmail())).thenReturn("123456");

        authService.loginWithOtp(req);

        assertEquals(0, user.getFailedLoginAttempts());
        verify(mailService).sendOtpEmail(req.getEmail(), "123456");
        verify(userRepository).save(user);
    }

    @Test
    void loginWithOtp_Fails_IncrementsAttemptsAndLocks() {
        LoginRequest req = new LoginRequest();
        req.setEmail("test@example.com");
        req.setPassword("wrongpass");
        user.setFailedLoginAttempts(4);
        when(userRepository.findByEmail(req.getEmail())).thenReturn(Optional.of(user));
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Bad credentials"));

        AppException ex = assertThrows(AppException.class, () -> authService.loginWithOtp(req));

        assertEquals(ErrorCode.INVALID_CREDENTIALS, ex.getErrorCode());
        assertEquals(5, user.getFailedLoginAttempts());
        assertEquals(UserStatus.LOCKED, user.getStatus());
        assertNotNull(user.getLockedAt());
        verify(userRepository).save(user);
    }

    @Test
    void verifyOtp_Success_ReturnsTokens() {
        OtpVerifyRequest req = new OtpVerifyRequest();
        req.setEmail("test@example.com");
        req.setOtp("123456");
        when(otpService.verifyOtp(req.getEmail(), req.getOtp())).thenReturn(true);
        when(userRepository.findByEmail(req.getEmail())).thenReturn(Optional.of(user));
        when(jwtService.generateToken(any(UserDetailsImpl.class))).thenReturn("access_token");
        when(jwtService.generateRefreshToken(any(), anyString())).thenReturn("refresh_token");

        TokenPair tokens = authService.verifyOtp(req);

        assertEquals("access_token", tokens.getAccessToken());
        assertEquals("refresh_token", tokens.getRefreshToken());
        assertNotNull(tokens.getCsrfToken());
        verify(refreshTokenService).saveRefreshToken(eq("refresh_token"), eq("test@example.com"), anyLong());
    }

    // ================= REFRESH =================

    @Test
    void refresh_Success() {
        String refreshToken = "valid_refresh";
        String csrfToken = "valid_csrf";
        when(jwtService.extractUsername(refreshToken)).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(jwtService.isTokenValid(eq(refreshToken), any(UserDetailsImpl.class))).thenReturn(true);
        when(refreshTokenService.isRefreshTokenValid(refreshToken)).thenReturn(true);
        when(jwtService.extractCsrfToken(refreshToken)).thenReturn(csrfToken);
        when(jwtService.generateToken(any(UserDetailsImpl.class))).thenReturn("new_access");

        AuthResponse resp = authService.refresh(refreshToken, csrfToken);

        assertEquals("new_access", resp.getAccessToken());
    }

    @Test
    void refresh_InvalidCsrfToken_ThrowsException() {
        String refreshToken = "valid_refresh";
        String csrfTokenHeader = "invalid_csrf";
        when(jwtService.extractUsername(refreshToken)).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(jwtService.isTokenValid(eq(refreshToken), any(UserDetailsImpl.class))).thenReturn(true);
        when(refreshTokenService.isRefreshTokenValid(refreshToken)).thenReturn(true);
        when(jwtService.extractCsrfToken(refreshToken)).thenReturn("correct_csrf");

        AppException ex = assertThrows(AppException.class, () -> authService.refresh(refreshToken, csrfTokenHeader));
        assertEquals(ErrorCode.INVALID_CSRF_TOKEN, ex.getErrorCode());
    }

    // ================= LOGOUT =================

    @Test
    void logout_Success() {
        String refreshToken = "valid_refresh";
        String csrfToken = "valid_csrf";
        when(jwtService.extractCsrfToken(refreshToken)).thenReturn(csrfToken);

        authService.logout(refreshToken, csrfToken);

        verify(refreshTokenService).revokeToken(refreshToken);
    }
}
