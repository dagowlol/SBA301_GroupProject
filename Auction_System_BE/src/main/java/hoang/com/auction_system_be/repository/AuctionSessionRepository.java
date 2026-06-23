package hoang.com.auction_system_be.repository;

import hoang.com.auction_system_be.entity.AuctionSession;
import hoang.com.auction_system_be.enums.SessionStatus;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuctionSessionRepository extends JpaRepository<AuctionSession, Long> {
    List<AuctionSession> findByStatusAndEndTimeBefore(
            SessionStatus status,
            LocalDateTime endTime);
}
