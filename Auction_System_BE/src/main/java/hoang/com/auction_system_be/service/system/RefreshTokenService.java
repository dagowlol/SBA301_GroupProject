package hoang.com.auction_system_be.service.system;
public interface RefreshTokenService {
    void saveRefreshToken(String token, String userEmail, long expiryMs);
    boolean isRefreshTokenValid(String token);
    void revokeToken(String token);
}
