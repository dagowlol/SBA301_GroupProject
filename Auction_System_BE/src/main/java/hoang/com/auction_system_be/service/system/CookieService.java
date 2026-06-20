package hoang.com.auction_system_be.service.system;
import jakarta.servlet.http.HttpServletResponse;
public interface CookieService {
    void addRefreshTokenCookie(HttpServletResponse response, String token);
    void addCsrfTokenCookie(HttpServletResponse response, String token);
    void clearCookies(HttpServletResponse response);
}
