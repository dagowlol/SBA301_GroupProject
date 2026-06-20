package hoang.com.auction_system_be.repository;
import hoang.com.auction_system_be.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.userEmail = :userEmail")
    void deleteAllByUserEmail(String userEmail);
}
