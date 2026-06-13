package hoang.com.auction_system_be.service.system;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.List;
public interface JwtService {
    String extractUsername(String token);
    String generateToken(UserDetails userDetails);
    String generateRefreshToken(UserDetails userDetails, String csrfToken);
    boolean isTokenValid(String token, UserDetails userDetails);
    List<SimpleGrantedAuthority> extractAuthorities(String token);
    String extractCsrfToken(String token);
}
