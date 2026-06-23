package hoang.com.auction_system_be.event;

import hoang.com.auction_system_be.dto.response.ErrorSocketResponse;
import hoang.com.auction_system_be.mapper.AuctionSessionMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class WebsocketNotifierListener {

    SimpMessagingTemplate messagingTemplate;
    AuctionSessionMapper auctionSessionMapper;

    @Async
    @EventListener
    public void onBidPlaced(BidPlacedEvent event) {
        if (event.isError()) {
            ErrorSocketResponse errorResponse = auctionSessionMapper.toErrorSocketResponse(event.getErrorMsg());
            messagingTemplate.convertAndSendToUser(
                    event.getUserId(), "/queue/errors", errorResponse);
        } else {
            messagingTemplate.convertAndSend(
                    "/topic/auction/" + event.getSessionId(),
                    (Object) event.getBroadcastResponse());
        }
    }

    @Async
    @EventListener
    public void onSessionExtended(SessionExtendedEvent event) {
        messagingTemplate.convertAndSend(
                "/topic/auction/" + event.getSessionId() + "/extended",
                (Object) event.getPayload());
        log.info("Broadcast SESSION_EXTENDED via event for session id={}", event.getSessionId());
    }

    @Async
    @EventListener
    public void onSessionStarted(SessionStartedEvent event) {
        messagingTemplate.convertAndSend(
                "/topic/auction/" + event.getSessionId() + "/status",
                "ACTIVE");
        log.info("Broadcast SESSION_STARTED via event for session id={}", event.getSessionId());
    }

    @Async
    @EventListener
    public void onSessionEnded(SessionEndedEvent event) {
        messagingTemplate.convertAndSend(
                "/topic/auction/" + event.getSessionId() + "/status",
                "ENDED");
        log.info("Broadcast SESSION_ENDED via event for session id={}", event.getSessionId());
    }
}
