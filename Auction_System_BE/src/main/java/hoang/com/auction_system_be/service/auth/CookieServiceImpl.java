package hoang.com.auction_system_be.service.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CookieServiceImpl implements CookieService {
    @Value("${jwt.refresh-token.expiration-ms}")
    private long refreshTokenExpirationMs;
    @Value("${app.cookie.secure:false}")
    private boolean isCookieSecure;
    private static final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";
    private static final String CSRF_TOKEN_COOKIE_NAME = "csrf_token";
    private static final String COOKIE_PATH = "/";

    @Override
    public void addRefreshTokenCookie(HttpServletResponse response, String token) {
        Cookie cookie = createCookie(REFRESH_TOKEN_COOKIE_NAME, token, true);
        response.addCookie(cookie);
    }

    @Override
    public void addCsrfTokenCookie(HttpServletResponse response, String token) {
        Cookie cookie = createCookie(CSRF_TOKEN_COOKIE_NAME, token, false);
        response.addCookie(cookie);
    }

    @Override
    public void clearCookies(HttpServletResponse response) {
        response.addCookie(createExpiredCookie(REFRESH_TOKEN_COOKIE_NAME));
        response.addCookie(createExpiredCookie(CSRF_TOKEN_COOKIE_NAME));
    }

    // ─── Private helpers ──────────────────────────────────────────────────
    private Cookie createCookie(String name, String value, boolean isHttpOnly) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(isHttpOnly);
        cookie.setSecure(isCookieSecure);
        cookie.setPath(COOKIE_PATH);
        cookie.setMaxAge((int) (refreshTokenExpirationMs / 1000));
        return cookie;
    }

    private Cookie createExpiredCookie(String name) {
        Cookie cookie = new Cookie(name, null);
        cookie.setHttpOnly(true);
        cookie.setSecure(isCookieSecure);
        cookie.setPath(COOKIE_PATH);
        cookie.setMaxAge(0);
        return cookie;
    }
}
