package hoang.com.auction_system_be.repository;

import hoang.com.auction_system_be.entity.AutoBidConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AutoBidConfigRepository extends JpaRepository<AutoBidConfig, Long> {

    Optional<AutoBidConfig> findByParticipantUserIdAndParticipantSessionId(Long userId, Long sessionId);

    List<AutoBidConfig> findByParticipantSessionIdAndIsActiveTrue(Long sessionId);
}
