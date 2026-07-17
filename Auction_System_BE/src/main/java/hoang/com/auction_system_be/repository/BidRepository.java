package hoang.com.auction_system_be.repository;

import hoang.com.auction_system_be.entity.Bid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.Optional;
import java.time.LocalDateTime;

@Repository
public interface BidRepository extends JpaRepository<Bid, Long>, JpaSpecificationExecutor<Bid> {

    List<Bid> findTop10ByParticipantSessionIdOrderByBidTimestampDesc(Long sessionId);

    Optional<Bid> findTopByParticipantUserIdAndParticipantSessionIdOrderByBidTimestampDesc(Long userId, Long sessionId);

    long countByBidTimestampGreaterThanEqualAndBidTimestampLessThanAndStatusNot(
            LocalDateTime fromDate, LocalDateTime toDate, hoang.com.auction_system_be.enums.BidStatus status);
}
