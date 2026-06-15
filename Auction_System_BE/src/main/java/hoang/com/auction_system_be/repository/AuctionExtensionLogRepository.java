package hoang.com.auction_system_be.repository;

import hoang.com.auction_system_be.entity.AuctionExtensionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuctionExtensionLogRepository extends JpaRepository<AuctionExtensionLog, Long> {
}
