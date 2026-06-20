package hoang.com.auction_system_be.repository;

import hoang.com.auction_system_be.entity.Bid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BidRepository extends JpaRepository<Bid, Long> {

    List<Bid> findTop10ByParticipantSessionIdOrderByBidTimestampDesc(Long sessionId);
}
