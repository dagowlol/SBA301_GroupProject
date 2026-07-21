package hoang.com.auction_system_be.repository;

import hoang.com.auction_system_be.entity.Shipping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShippingRepository extends JpaRepository<Shipping, Long>, JpaSpecificationExecutor<Shipping> {
    Optional<Shipping> findByItemId(Long itemId);
    List<Shipping> findByBuyerId(Long buyerId);
}
