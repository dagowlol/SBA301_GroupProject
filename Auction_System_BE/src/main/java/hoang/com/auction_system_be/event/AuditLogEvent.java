package hoang.com.auction_system_be.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class AuditLogEvent extends ApplicationEvent {

    public static final String ACTION_SESSION_CREATED = "SESSION_CREATED";
    public static final String ACTION_SESSION_UPDATED = "SESSION_UPDATED";
    public static final String ACTION_SESSION_DELETED = "SESSION_DELETED";
    public static final String ENTITY_AUCTION_SESSION = "AuctionSession";

    private final Long userId;
    private final String action;
    private final String entityType;
    private final Long entityId;
    private final String details;

    public AuditLogEvent(Object source, Long userId, String action, String entityType, Long entityId, String details) {
        super(source);
        this.userId = userId;
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.details = details;
    }
}
