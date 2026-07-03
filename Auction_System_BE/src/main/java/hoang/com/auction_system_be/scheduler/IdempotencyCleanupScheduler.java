package hoang.com.auction_system_be.scheduler;

import hoang.com.auction_system_be.repository.IdempotencyRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;


@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class IdempotencyCleanupScheduler {

    IdempotencyRepository idempotencyRepository;

    
    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void cleanupExpiredRecords() {
        int deletedCount = idempotencyRepository.deleteExpired(LocalDateTime.now());
        if (deletedCount > 0) {
            log.info("Cleaned up {} expired idempotency records", deletedCount);
        }
    }
}
