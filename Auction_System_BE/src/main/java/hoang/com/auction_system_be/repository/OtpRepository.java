package hoang.com.auction_system_be.repository;
import hoang.com.auction_system_be.entity.OtpRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
@Repository
public interface OtpRepository extends JpaRepository<OtpRecord, Long> {
    Optional<OtpRecord> findByEmailAndOtpCodeAndUsedFalse(String email, String otpCode);
    void deleteByEmail(String email);
}
