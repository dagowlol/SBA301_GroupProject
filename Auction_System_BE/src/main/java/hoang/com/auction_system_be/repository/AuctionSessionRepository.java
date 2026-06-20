package hoang.com.auction_system_be.repository;

import hoang.com.auction_system_be.entity.AuctionSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuctionSessionRepository extends JpaRepository<AuctionSession, Long> {
}
