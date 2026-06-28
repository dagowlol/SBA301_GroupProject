package hoang.com.auction_system_be.event;

import hoang.com.auction_system_be.entity.AuditLog;
import hoang.com.auction_system_be.repository.AuditLogRepository;
import hoang.com.auction_system_be.repository.UserRepository;
import hoang.com.auction_system_be.entity.User;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AuditLogListener {

    AuditLogRepository auditLogRepository;
    UserRepository userRepository;

    
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onAuditLogEvent(AuditLogEvent event) {
        try {
            User userRef = event.getUserId() != null ? userRepository.getReferenceById(event.getUserId()) : null;
            auditLogRepository.save(AuditLog.builder()
                    .user(userRef)
                    .action(event.getAction())
                    .entityType(event.getEntityType())
                    .entityId(event.getEntityId())
                    .details(event.getDetails())
                    .build());
            log.debug("Audit log saved async: {} for {} id={}", event.getAction(), event.getEntityType(), event.getEntityId());
        } catch (Exception e) {
            log.error("Failed to save async audit log", e);
        }
    }
}
