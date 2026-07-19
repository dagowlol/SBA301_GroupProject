package hoang.com.auction_system_be.repository;

import hoang.com.auction_system_be.dto.response.AuctionSessionListResponse;
import hoang.com.auction_system_be.dto.response.EarningTransactionResponse;
import hoang.com.auction_system_be.entity.AuctionSession;
import hoang.com.auction_system_be.enums.SessionStatus;
import hoang.com.auction_system_be.repository.projection.CategoryAuctionSuccessProjection;
import hoang.com.auction_system_be.repository.projection.EarningSummaryProjection;
import hoang.com.auction_system_be.repository.projection.EarningRevenuePointProjection;
import hoang.com.auction_system_be.repository.projection.EarningStatusCountProjection;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuctionSessionRepository extends JpaRepository<AuctionSession, Long>,
        JpaSpecificationExecutor<AuctionSession> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM AuctionSession s WHERE s.id = :id")
    Optional<AuctionSession> findByIdWithPessimisticLock(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM AuctionSession s WHERE s.id = :id")
    Optional<AuctionSession> findByIdForUpdate(@Param("id") Long id);

    @EntityGraph(attributePaths = {
            "item", "item.images",
            "currentWinnerParticipant", "currentWinnerParticipant.user",
            "createdBy"
    })
    @Query("SELECT s FROM AuctionSession s WHERE s.id = :id")
    Optional<AuctionSession> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT s.id FROM AuctionSession s WHERE s.status = :status AND s.startTime <= :time")
    Page<Long> findIdsByStatusAndStartTimeBefore(@Param("status") SessionStatus status,
            @Param("time") LocalDateTime time, Pageable pageable);

    @Query("SELECT s.id FROM AuctionSession s WHERE s.status = :status AND s.endTime <= :time")
    Page<Long> findIdsByStatusAndEndTimeBefore(@Param("status") SessionStatus status,
            @Param("time") LocalDateTime time, Pageable pageable);

    @Modifying
    @Transactional
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
                WHERE (:hasCursor = false OR s.id < :cursor)
                  AND (:hasStatus = false OR s.status = :status)
                  AND (:hasSearch = false OR LOWER(i.name) LIKE LOWER(CONCAT('%', :search, '%')))
                ORDER BY s.id DESC
            """)
    List<AuctionSessionListResponse> findOptimizedSessions(
            @Param("hasCursor") boolean hasCursor,
            @Param("cursor") Long cursor,
            @Param("hasStatus") boolean hasStatus,
            @Param("status") SessionStatus status,
            @Param("hasSearch") boolean hasSearch,
            @Param("search") String search,
            Pageable pageable);

    @Query(value = "SELECT s.id, i.id as itemId, i.name as itemName, i.description, s.reserve_price as reservePrice, s.current_highest_bid as currentHighestBid, s.status, s.start_time as startTime, s.end_time as endTime FROM auction_sessions s JOIN auction_items i ON i.id = s.item_id WHERE s.deleted_at IS NOT NULL ORDER BY s.id DESC", nativeQuery = true)
    List<Object[]> findDeletedSessionsRaw();

    @Modifying
    @Transactional
    @Query(value = "UPDATE auction_sessions SET deleted_at = NULL WHERE id = :id", nativeQuery = true)
    int restoreSession(@Param("id") Long id);

    @Query("SELECT COUNT(s) FROM AuctionSession s WHERE s.status = :status " +
            "AND s.startTime < :toDate AND s.endTime >= :fromDate")
    long countRunningSessionsInRange(@Param("status") SessionStatus status,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate);

    @Query("SELECT COUNT(s) FROM AuctionSession s " +
            "WHERE s.status = hoang.com.auction_system_be.enums.SessionStatus.ENDED " +
            "AND s.currentWinnerParticipant IS NOT NULL " +
            "AND s.endTime >= :fromDate AND s.endTime < :toDate")
    long countCompletedSessionsInRange(@Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate);

    @Query(value = """
            SELECT c.id AS categoryId, c.name AS categoryName, COUNT(DISTINCT s.id) AS successfulAuctions
            FROM auction_sessions s
            JOIN auction_items i ON i.id = s.item_id AND i.deleted_at IS NULL
            JOIN categories c ON c.id = i.category_id AND c.deleted_at IS NULL
            JOIN payments p ON p.participant_id = s.current_winner_participant_id
                           AND p.type = 'FINAL_PAYMENT'
                           AND p.status = 'PAID'
                           AND p.deleted_at IS NULL
            WHERE s.status = 'ENDED'
              AND s.deleted_at IS NULL
              AND s.end_time >= :fromDate
              AND s.end_time < :toDate
            GROUP BY c.id, c.name
            ORDER BY successfulAuctions DESC, c.name ASC
            """, nativeQuery = true)
    List<CategoryAuctionSuccessProjection> getSuccessfulAuctionsByCategory(
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate);

    @Query(value = """
                SELECT
                    COALESCE(SUM(CASE WHEN p.status = 'PAID' THEN s.current_highest_bid ELSE 0 END), 0) AS totalRevenue,
                    COALESCE(SUM(CAST(CASE WHEN p.status = 'PAID' THEN 1 ELSE 0 END AS BIGINT)), 0) AS successfulProducts,
                    COALESCE(SUM(CASE WHEN p.id IS NULL OR p.status <> 'PAID' THEN s.current_highest_bid ELSE 0 END), 0) AS pendingAmount
                FROM auction_sessions s
                JOIN auction_items i ON i.id = s.item_id AND i.deleted_at IS NULL
                LEFT JOIN payments p
                  ON p.participant_id = s.current_winner_participant_id
                 AND p.type = 'FINAL_PAYMENT'
                 AND p.deleted_at IS NULL
                WHERE i.seller_id = :sellerId
                  AND s.status = 'ENDED'
                  AND s.current_winner_participant_id IS NOT NULL
                  AND s.deleted_at IS NULL
            """, nativeQuery = true)
    EarningSummaryProjection getEarningSummary(@Param("sellerId") Long sellerId);

    @Query(value = """
                SELECT
                    CONVERT(VARCHAR(10), CAST(s.end_time AS DATE), 23) AS period,
                    COALESCE(SUM(CASE WHEN p.status = 'PAID' THEN s.current_highest_bid ELSE 0 END), 0) AS revenue
                FROM auction_sessions s
                JOIN auction_items i ON i.id = s.item_id AND i.deleted_at IS NULL
                LEFT JOIN payments p
                  ON p.participant_id = s.current_winner_participant_id
                 AND p.type = 'FINAL_PAYMENT'
                 AND p.deleted_at IS NULL
                WHERE i.seller_id = :sellerId
                  AND s.status = 'ENDED'
                  AND s.current_winner_participant_id IS NOT NULL
                  AND s.deleted_at IS NULL
                  AND s.end_time >= :fromDate
                  AND s.end_time < :toDate
                GROUP BY CAST(s.end_time AS DATE)
                ORDER BY CAST(s.end_time AS DATE)
            """, nativeQuery = true)
    List<EarningRevenuePointProjection> getDailyEarningRevenue(
            @Param("sellerId") Long sellerId,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate);

    @Query(value = """
                SELECT
                    CONCAT(YEAR(s.end_time), '-', RIGHT(CONCAT('0', MONTH(s.end_time)), 2)) AS period,
                    COALESCE(SUM(CASE WHEN p.status = 'PAID' THEN s.current_highest_bid ELSE 0 END), 0) AS revenue
                FROM auction_sessions s
                JOIN auction_items i ON i.id = s.item_id AND i.deleted_at IS NULL
                LEFT JOIN payments p
                  ON p.participant_id = s.current_winner_participant_id
                 AND p.type = 'FINAL_PAYMENT'
                 AND p.deleted_at IS NULL
                WHERE i.seller_id = :sellerId
                  AND s.status = 'ENDED'
                  AND s.current_winner_participant_id IS NOT NULL
                  AND s.deleted_at IS NULL
                  AND s.end_time >= :fromDate
                  AND s.end_time < :toDate
                GROUP BY YEAR(s.end_time), MONTH(s.end_time)
                ORDER BY YEAR(s.end_time), MONTH(s.end_time)
            """, nativeQuery = true)
    List<EarningRevenuePointProjection> getMonthlyEarningRevenue(
            @Param("sellerId") Long sellerId,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate);

    @Query(value = """
                SELECT
                    COALESCE(SUM(CAST(CASE WHEN p.status = 'PAID' THEN 1 ELSE 0 END AS BIGINT)), 0) AS paidCount,
                    COALESCE(SUM(CAST(CASE WHEN p.id IS NULL OR p.status <> 'PAID' THEN 1 ELSE 0 END AS BIGINT)), 0) AS pendingCount
                FROM auction_sessions s
                JOIN auction_items i ON i.id = s.item_id AND i.deleted_at IS NULL
                LEFT JOIN payments p
                  ON p.participant_id = s.current_winner_participant_id
                 AND p.type = 'FINAL_PAYMENT'
                 AND p.deleted_at IS NULL
                WHERE i.seller_id = :sellerId
                  AND s.status = 'ENDED'
                  AND s.current_winner_participant_id IS NOT NULL
                  AND s.deleted_at IS NULL
                  AND s.end_time >= :fromDate
                  AND s.end_time < :toDate
            """, nativeQuery = true)
    EarningStatusCountProjection getEarningStatusCounts(
            @Param("sellerId") Long sellerId,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate);

    @Query("""
                SELECT new hoang.com.auction_system_be.dto.response.EarningTransactionResponse(
                    s.id, i.name, s.endTime, s.currentHighestBid,
                    u.firstName, u.lastName, p.status
                )
                FROM AuctionSession s
                JOIN s.item i
                JOIN s.currentWinnerParticipant wp
                JOIN wp.user u
                LEFT JOIN Payment p ON p.participant = wp
                    AND p.type = hoang.com.auction_system_be.enums.PaymentType.FINAL_PAYMENT
                    AND p.id = (
                        SELECT MAX(p2.id)
                        FROM Payment p2
                        WHERE p2.participant = wp
                          AND p2.type = hoang.com.auction_system_be.enums.PaymentType.FINAL_PAYMENT
                    )
                WHERE i.seller.id = :sellerId
                  AND s.status = hoang.com.auction_system_be.enums.SessionStatus.ENDED
                  AND s.currentWinnerParticipant IS NOT NULL
                  AND (:hasCursor = false OR s.id < :cursor)
                  AND (
                     :status = 'ALL' OR
                     (:status = 'SUCCESS' AND p.status = hoang.com.auction_system_be.enums.PaymentStatus.PAID) OR
                     (:status = 'PENDING' AND (p.id IS NULL OR p.status != hoang.com.auction_system_be.enums.PaymentStatus.PAID))
                  )
                ORDER BY s.id DESC
            """)
    List<EarningTransactionResponse> findOptimizedEarningTransactions(
            @Param("sellerId") Long sellerId,
            @Param("hasCursor") boolean hasCursor,
            @Param("cursor") Long cursor,
            @Param("status") String status,
            Pageable pageable);
}
