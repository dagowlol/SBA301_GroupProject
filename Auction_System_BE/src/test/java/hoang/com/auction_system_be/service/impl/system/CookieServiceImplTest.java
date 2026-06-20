package hoang.com.auction_system_be.service.impl.system;

import hoang.com.auction_system_be.service.system.*;

import hoang.com.auction_system_be.service.impl.system.CookieServiceImpl;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CookieServiceImplTest {

    @Mock
    private HttpServletResponse response;

    private CookieServiceImpl cookieService;

    @BeforeEach
    void setUp() {
        cookieService = new CookieServiceImpl();
        ReflectionTestUtils.setField(cookieService, "refreshTokenExpirationMs", 3600000L); // 1 hour
        ReflectionTestUtils.setField(cookieService, "isCookieSecure", true);
    }

    @Test
    void addRefreshTokenCookie_Success() {
        String token = "refresh.token.value";

        cookieService.addRefreshTokenCookie(response, token);

        ArgumentCaptor<Cookie> cookieCaptor = ArgumentCaptor.forClass(Cookie.class);
        verify(response, times(1)).addCookie(cookieCaptor.capture());

        Cookie capturedCookie = cookieCaptor.getValue();
        assertEquals("refresh_token", capturedCookie.getName());
        assertEquals(token, capturedCookie.getValue());
        assertTrue(capturedCookie.isHttpOnly());
        assertTrue(capturedCookie.getSecure());
        assertEquals("/", capturedCookie.getPath());
        assertEquals(3600, capturedCookie.getMaxAge()); // 3600000 ms / 1000
    }

    @Test
    void addCsrfTokenCookie_Success() {
        String token = "csrf.token.value";

        cookieService.addCsrfTokenCookie(response, token);

        ArgumentCaptor<Cookie> cookieCaptor = ArgumentCaptor.forClass(Cookie.class);
        verify(response, times(1)).addCookie(cookieCaptor.capture());

        Cookie capturedCookie = cookieCaptor.getValue();
        assertEquals("csrf_token", capturedCookie.getName());
        assertEquals(token, capturedCookie.getValue());
        assertFalse(capturedCookie.isHttpOnly()); // CSRF token should be accessible via JS
        assertTrue(capturedCookie.getSecure());
        assertEquals("/", capturedCookie.getPath());
        assertEquals(3600, capturedCookie.getMaxAge());
    }

    @Test
    void clearCookies_Success() {
        cookieService.clearCookies(response);

        ArgumentCaptor<Cookie> cookieCaptor = ArgumentCaptor.forClass(Cookie.class);
        verify(response, times(2)).addCookie(cookieCaptor.capture());

        Cookie refreshTokenCookie = cookieCaptor.getAllValues().get(0);
        assertEquals("refresh_token", refreshTokenCookie.getName());
        assertNull(refreshTokenCookie.getValue());
        assertEquals(0, refreshTokenCookie.getMaxAge());
        assertTrue(refreshTokenCookie.isHttpOnly());

        Cookie csrfTokenCookie = cookieCaptor.getAllValues().get(1);
        assertEquals("csrf_token", csrfTokenCookie.getName());
        assertNull(csrfTokenCookie.getValue());
        assertEquals(0, csrfTokenCookie.getMaxAge());
    }
}
