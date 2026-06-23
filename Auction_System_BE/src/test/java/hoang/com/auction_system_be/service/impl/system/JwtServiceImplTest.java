package hoang.com.auction_system_be.service.impl.system;

import hoang.com.auction_system_be.service.auth.JwtServiceImpl;

import hoang.com.auction_system_be.security.UserDetailsImpl;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JwtServiceImplTest {

    private JwtServiceImpl jwtService;
    private UserDetails userDetails;

    // 256-bit secret key in base64
    private final String SECRET_KEY = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

    @BeforeEach
    void setUp() {
        jwtService = new JwtServiceImpl();
        ReflectionTestUtils.setField(jwtService, "jwtSecretKey", SECRET_KEY);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 3600000L); // 1 hour
        ReflectionTestUtils.setField(jwtService, "jwtRefreshExpiration", 86400000L); // 24 hours
        ReflectionTestUtils.setField(jwtService, "jwtIssuer", "test-issuer");
        ReflectionTestUtils.setField(jwtService, "jwtAudience", "test-audience");

        // Use UserDetailsImpl to cover the extra claims mapping
        userDetails = mock(UserDetailsImpl.class);
        lenient().when(userDetails.getUsername()).thenReturn("test@example.com");

        Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        lenient().doReturn(authorities).when(userDetails).getAuthorities();
        lenient().when(((UserDetailsImpl) userDetails).getId()).thenReturn(1L);
    }

    @Test
    void generateToken_Success() {
        String token = jwtService.generateToken(userDetails);

        assertNotNull(token);
        assertFalse(token.isEmpty());

        String username = jwtService.extractUsername(token);
        assertEquals("test@example.com", username);

        assertTrue(jwtService.isTokenValid(token, userDetails));

        List<SimpleGrantedAuthority> extractedAuthorities = jwtService.extractAuthorities(token);
        assertEquals(1, extractedAuthorities.size());
        assertEquals("ROLE_USER", extractedAuthorities.get(0).getAuthority());
    }

    @Test
    void generateRefreshToken_Success() {
        String csrfToken = "random-csrf-token";
        String token = jwtService.generateRefreshToken(userDetails, csrfToken);

        assertNotNull(token);

        String username = jwtService.extractUsername(token);
        assertEquals("test@example.com", username);

        String extractedCsrf = jwtService.extractCsrfToken(token);
        assertEquals(csrfToken, extractedCsrf);

        assertTrue(jwtService.isTokenValid(token, userDetails));
    }

    @Test
    void isTokenValid_False_WhenUsernameDiffers() {
        String token = jwtService.generateToken(userDetails);

        UserDetails anotherUser = mock(UserDetails.class);
        when(anotherUser.getUsername()).thenReturn("different@example.com");

        assertFalse(jwtService.isTokenValid(token, anotherUser));
    }

    @Test
    void extractAuthorities_EmptyWhenNoRoles() {
        // Create a new mock user with no authorities
        UserDetails userNoRoles = mock(UserDetails.class);
        when(userNoRoles.getUsername()).thenReturn("noroles@example.com");
        doReturn(List.of()).when(userNoRoles).getAuthorities();

        String token = jwtService.generateToken(userNoRoles);
        List<SimpleGrantedAuthority> authorities = jwtService.extractAuthorities(token);

        assertTrue(authorities.isEmpty());
    }

    @Test
    void verifySignature_FailsWithDifferentKey() {
        String token = jwtService.generateToken(userDetails);

        // Create a new JwtService with a different key
        JwtServiceImpl anotherJwtService = new JwtServiceImpl();
        ReflectionTestUtils.setField(anotherJwtService, "jwtSecretKey",
                "505E635266556A586E3272357538782F413F4428472B4B6250645367566B5971");
        ReflectionTestUtils.setField(anotherJwtService, "jwtIssuer", "test-issuer");

        assertThrows(SignatureException.class, () -> anotherJwtService.extractUsername(token));
    }
}
