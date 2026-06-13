package hoang.com.auction_system_be.service.impl.system;
import hoang.com.auction_system_be.entity.RefreshToken;
import hoang.com.auction_system_be.repository.RefreshTokenRepository;
import hoang.com.auction_system_be.service.system.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    @Override
    @Transactional
    public void saveRefreshToken(String token, String userEmail, long expiryMs) {
        RefreshToken refreshToken = RefreshToken.builder()
                .token(token)
                .userEmail(userEmail)
                .expiryTime(Instant.now().plusMillis(expiryMs))
                .revoked(false)
                .build();
        refreshTokenRepository.save(refreshToken);
    }
    @Override
    @Transactional(readOnly = true)
    public boolean isRefreshTokenValid(String token) {
        return refreshTokenRepository.findByToken(token)
                .filter(rt -> !rt.isRevoked() && rt.getExpiryTime().isAfter(Instant.now()))
                .isPresent();
    }
    @Override
    @Transactional
    public void revokeToken(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(rt -> {
            rt.setRevoked(true);
            refreshTokenRepository.save(rt);
        });
    }
}
