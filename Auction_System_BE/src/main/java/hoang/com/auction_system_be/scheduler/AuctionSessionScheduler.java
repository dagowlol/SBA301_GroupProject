package hoang.com.auction_system_be.scheduler;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import hoang.com.auction_system_be.entity.AuctionSession;
import hoang.com.auction_system_be.enums.SessionStatus;
import hoang.com.auction_system_be.repository.AuctionSessionRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuctionSessionScheduler {

    AuctionSessionRepository auctionSessionRepository;
    SimpMessagingTemplate messagingTemplate;

    @Scheduled(fixedRate = 5000)
    @Transactional
    public void closeExpiredSessions() {

        List<AuctionSession> sessions = auctionSessionRepository
                .findByStatusAndEndTimeBefore(
                        SessionStatus.ACTIVE,
                        LocalDateTime.now());

        for (AuctionSession session : sessions) {

            session.setStatus(SessionStatus.ENDED);

            messagingTemplate.convertAndSend(
                    "/topic/auction/" + session.getId() + "/ended",
                    "Auction ended");
        }
    }
}