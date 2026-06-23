package hoang.com.auction_system_be.repository;

import hoang.com.auction_system_be.entity.AuctionSession;
import hoang.com.auction_system_be.enums.SessionStatus;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuctionSessionRepository extends JpaRepository<AuctionSession, Long>,
                JpaSpecificationExecutor<AuctionSession> {

        @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
        @Query("SELECT s FROM AuctionSession s WHERE s.id = :id")
        Optional<AuctionSession> findByIdWithPessimisticLock(@Param("id") Long id);

        @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {
                "item", "item.images", 
                "currentWinnerParticipant", "currentWinnerParticipant.user", 
                "createdBy"
        })
        @Query("SELECT s FROM AuctionSession s WHERE s.id = :id")
        Optional<AuctionSession> findByIdWithDetails(@Param("id") Long id);

        @Query("SELECT s.id FROM AuctionSession s WHERE s.status = :status AND s.startTime <= :time")
        Page<Long> findIdsByStatusAndStartTimeBefore(@Param("status") SessionStatus status, @Param("time") LocalDateTime time, Pageable pageable);

        @Query("SELECT s.id FROM AuctionSession s WHERE s.status = :status AND s.endTime <= :time")
        Page<Long> findIdsByStatusAndEndTimeBefore(@Param("status") SessionStatus status, @Param("time") LocalDateTime time, Pageable pageable);

        @org.springframework.data.jpa.repository.Modifying
        @org.springframework.transaction.annotation.Transactional
        @Query("UPDATE AuctionSession s SET s.status = :newStatus WHERE s.id IN :ids")
        int updateStatusForIds(@Param("newStatus") SessionStatus newStatus, @Param("ids") List<Long> ids);



        
        List<AuctionSession> findByStatusAndStartTimeBefore(
                        SessionStatus status,
                        LocalDateTime startTime);

        
        boolean existsByItemIdAndStatusIn(Long itemId, List<SessionStatus> statuses);

        
        boolean existsByItemIdAndStatusInAndIdNot(Long itemId, List<SessionStatus> statuses, Long excludeId);

        @Query("""
            SELECT new hoang.com.auction_system_be.dto.response.AuctionSessionListResponse(
                s.id, i.id, i.name, i.description, 
                s.reservePrice, s.currentHighestBid, s.status, s.startTime, s.endTime
            )
            FROM AuctionSession s
            JOIN s.item i
            WHERE (:cursor IS NULL OR s.id < :cursor)
              AND (:hasStatus = false OR s.status = :status)
              AND (:hasSearch = false OR LOWER(i.name) LIKE LOWER(CONCAT('%', :search, '%')))
            ORDER BY s.id DESC
        """)
        List<hoang.com.auction_system_be.dto.response.AuctionSessionListResponse> findOptimizedSessions(
                @Param("cursor") Long cursor,
                @Param("hasStatus") boolean hasStatus,
                @Param("status") SessionStatus status,
                @Param("hasSearch") boolean hasSearch,
                @Param("search") String search,
                Pageable pageable);
}
