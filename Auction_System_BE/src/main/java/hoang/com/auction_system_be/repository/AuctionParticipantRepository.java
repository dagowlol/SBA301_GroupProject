package hoang.com.auction_system_be.repository;

import hoang.com.auction_system_be.entity.AuctionParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuctionParticipantRepository extends JpaRepository<AuctionParticipant, Long> {

    Optional<AuctionParticipant> findByUserIdAndSessionId(Long userId, Long sessionId);
}
