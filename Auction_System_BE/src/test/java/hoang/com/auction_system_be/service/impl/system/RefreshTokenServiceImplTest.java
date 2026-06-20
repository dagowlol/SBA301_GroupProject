package hoang.com.auction_system_be.service.impl.system;

import hoang.com.auction_system_be.service.system.*;

import hoang.com.auction_system_be.entity.RefreshToken;
import hoang.com.auction_system_be.repository.RefreshTokenRepository;
import hoang.com.auction_system_be.service.impl.system.RefreshTokenServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RefreshTokenServiceImplTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private RefreshTokenServiceImpl refreshTokenService;

    private RefreshToken validToken;
    private RefreshToken expiredToken;
    private RefreshToken revokedToken;

    @BeforeEach
    void setUp() {
        validToken = RefreshToken.builder()
                .token("valid-token")
                .userEmail("test@example.com")
                .expiryTime(Instant.now().plusSeconds(3600))
                .revoked(false)
                .build();

        expiredToken = RefreshToken.builder()
                .token("expired-token")
                .userEmail("test@example.com")
                .expiryTime(Instant.now().minusSeconds(3600))
                .revoked(false)
                .build();

        revokedToken = RefreshToken.builder()
                .token("revoked-token")
                .userEmail("test@example.com")
                .expiryTime(Instant.now().plusSeconds(3600))
                .revoked(true)
                .build();
    }

    @Test
    void saveRefreshToken_Success() {
        String token = "new-token";
        String email = "test@example.com";
        long expiryMs = 3600000;

        refreshTokenService.saveRefreshToken(token, email, expiryMs);

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository, times(1)).save(captor.capture());
        
        RefreshToken savedToken = captor.getValue();
        assertEquals(token, savedToken.getToken());
        assertEquals(email, savedToken.getUserEmail());
        assertFalse(savedToken.isRevoked());
        assertTrue(savedToken.getExpiryTime().isAfter(Instant.now()));
    }

    @Test
    void isRefreshTokenValid_True_WhenTokenIsValid() {
        when(refreshTokenRepository.findByToken("valid-token")).thenReturn(Optional.of(validToken));
        
        boolean isValid = refreshTokenService.isRefreshTokenValid("valid-token");
        
        assertTrue(isValid);
    }

    @Test
    void isRefreshTokenValid_False_WhenTokenNotFound() {
        when(refreshTokenRepository.findByToken("non-existent-token")).thenReturn(Optional.empty());
        
        boolean isValid = refreshTokenService.isRefreshTokenValid("non-existent-token");
        
        assertFalse(isValid);
    }

    @Test
    void isRefreshTokenValid_False_WhenTokenIsExpired() {
        when(refreshTokenRepository.findByToken("expired-token")).thenReturn(Optional.of(expiredToken));
        
        boolean isValid = refreshTokenService.isRefreshTokenValid("expired-token");
        
        assertFalse(isValid);
    }

    @Test
    void isRefreshTokenValid_False_WhenTokenIsRevoked() {
        when(refreshTokenRepository.findByToken("revoked-token")).thenReturn(Optional.of(revokedToken));
        
        boolean isValid = refreshTokenService.isRefreshTokenValid("revoked-token");
        
        assertFalse(isValid);
    }

    @Test
    void revokeToken_Success_WhenTokenExists() {
        when(refreshTokenRepository.findByToken("valid-token")).thenReturn(Optional.of(validToken));
        
        refreshTokenService.revokeToken("valid-token");
        
        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository, times(1)).save(captor.capture());
        
        assertTrue(captor.getValue().isRevoked());
    }

    @Test
    void revokeToken_DoesNothing_WhenTokenNotFound() {
        when(refreshTokenRepository.findByToken("non-existent-token")).thenReturn(Optional.empty());
        
        refreshTokenService.revokeToken("non-existent-token");
        
        verify(refreshTokenRepository, never()).save(any());
    }
}
