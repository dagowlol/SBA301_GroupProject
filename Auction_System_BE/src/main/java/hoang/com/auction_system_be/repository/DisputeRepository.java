package hoang.com.auction_system_be.repository;

import hoang.com.auction_system_be.entity.Dispute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface DisputeRepository extends JpaRepository<Dispute, Long>, JpaSpecificationExecutor<Dispute> {
}
