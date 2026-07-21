package hoang.com.auction_system_be.repository;

import hoang.com.auction_system_be.entity.Payment;
import hoang.com.auction_system_be.enums.PaymentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import hoang.com.auction_system_be.repository.projection.AnalyticsRevenuePointProjection;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByTransactionId(String transactionId);

    Optional<Payment> findByPaymentGatewayRef(String paymentGatewayRef);

    Optional<Payment> findByParticipantIdAndType(Long participantId, PaymentType type);

    Optional<Payment> findByParticipantUserIdAndParticipantSessionId(Long userId, Long sessionId);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p " +
            "WHERE p.status = hoang.com.auction_system_be.enums.PaymentStatus.PAID " +
            "AND p.type = hoang.com.auction_system_be.enums.PaymentType.FINAL_PAYMENT " +
            "AND p.paidAt >= :fromDate AND p.paidAt < :toDate")
    BigDecimal sumPaidRevenue(@Param("fromDate") LocalDateTime fromDate,
                              @Param("toDate") LocalDateTime toDate);

    @Query(value = """
            SELECT CONVERT(VARCHAR(10), CAST(p.paid_at AS DATE), 23) AS period,
                   COALESCE(SUM(p.amount), 0) AS revenue
            FROM payments p
            WHERE p.status = 'PAID'
              AND p.type = 'FINAL_PAYMENT'
              AND p.deleted_at IS NULL
              AND p.paid_at >= :fromDate
              AND p.paid_at < :toDate
            GROUP BY CAST(p.paid_at AS DATE)
            ORDER BY CAST(p.paid_at AS DATE)
            """, nativeQuery = true)
    List<AnalyticsRevenuePointProjection> getDailyPaidRevenue(
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate);

    @Query(value = """
            SELECT CONVERT(VARCHAR(10), CAST(p.paid_at AS DATE), 23) AS period,
                   COALESCE(SUM(p.amount), 0) AS revenue
            FROM payments p
            WHERE p.status = 'PAID'
              AND p.type = 'FINAL_PAYMENT'
              AND p.deleted_at IS NULL
              AND p.paid_at IS NOT NULL
            GROUP BY CAST(p.paid_at AS DATE)
            ORDER BY CAST(p.paid_at AS DATE)
            """, nativeQuery = true)
    List<AnalyticsRevenuePointProjection> getAllPaidRevenueByDay();

    @Query(value = """
            SELECT CONVERT(VARCHAR(10),
                           DATEADD(WEEK, DATEDIFF(WEEK, 0, p.paid_at), 0), 23) AS period,
                   COALESCE(SUM(p.amount), 0) AS revenue
            FROM payments p
            WHERE p.status = 'PAID'
              AND p.type = 'FINAL_PAYMENT'
              AND p.deleted_at IS NULL
              AND p.paid_at IS NOT NULL
            GROUP BY DATEADD(WEEK, DATEDIFF(WEEK, 0, p.paid_at), 0)
            ORDER BY DATEADD(WEEK, DATEDIFF(WEEK, 0, p.paid_at), 0)
            """, nativeQuery = true)
    List<AnalyticsRevenuePointProjection> getAllPaidRevenueByWeek();

    @Query(value = """
            SELECT CONVERT(VARCHAR(7), p.paid_at, 120) AS period,
                   COALESCE(SUM(p.amount), 0) AS revenue
            FROM payments p
            WHERE p.status = 'PAID'
              AND p.type = 'FINAL_PAYMENT'
              AND p.deleted_at IS NULL
              AND p.paid_at IS NOT NULL
            GROUP BY CONVERT(VARCHAR(7), p.paid_at, 120)
            ORDER BY CONVERT(VARCHAR(7), p.paid_at, 120)
            """, nativeQuery = true)
    List<AnalyticsRevenuePointProjection> getAllPaidRevenueByMonth();
}
