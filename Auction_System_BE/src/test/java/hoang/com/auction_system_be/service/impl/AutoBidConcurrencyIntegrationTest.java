package hoang.com.auction_system_be.service.impl;

import hoang.com.auction_system_be.entity.*;
import hoang.com.auction_system_be.enums.SessionStatus;
import hoang.com.auction_system_be.repository.*;
import hoang.com.auction_system_be.service.autobid.AutoBidService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("schema")
public class AutoBidConcurrencyIntegrationTest {

    @Autowired
    AutoBidService autoBidService;

    @Autowired
    AutoBidConfigRepository autoBidConfigRepository;

    @Autowired
    BidRepository bidRepository;

    @Autowired
    AuctionParticipantRepository auctionParticipantRepository;

    @Autowired
    AuctionSessionRepository auctionSessionRepository;

    @Autowired
    AuctionItemRepository auctionItemRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    AuctionExtensionLogRepository auctionExtensionLogRepository;

    @Autowired
    CategoryRepository categoryRepository;

    private AuctionSession session;

    @BeforeEach
    void setUp() {
        // Cleanup existing test data
        autoBidConfigRepository.deleteAll();
        bidRepository.deleteAll();
        auctionExtensionLogRepository.deleteAll();
        auctionParticipantRepository.deleteAll();
        auctionSessionRepository.deleteAll();
        auctionItemRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();

        // Create standard staff user
        User staff = User.builder()
                .firstName("Staff")
                .lastName("Member")
                .email("staff@example.com")
                .password("123456")
                .build();
        staff = userRepository.save(staff);

        // Create category
        Category category = Category.builder()
                .name("Watches")
                .description("Vintage timepieces")
                .sortOrder(1)
                .build();
        category = categoryRepository.save(category);

        // Create an item
        AuctionItem item = AuctionItem.builder()
                .name("Vintage Watch")
                .description("An old vintage watch")
                .category(category)
                .seller(staff)
                .startingPrice(BigDecimal.valueOf(100))
                .status(hoang.com.auction_system_be.enums.ItemStatus.APPROVED)
                .build();
        item = auctionItemRepository.save(item);

        // Create session
        session = AuctionSession.builder()
                .item(item)
                .startTime(LocalDateTime.now().minusHours(1))
                .endTime(LocalDateTime.now().plusHours(1))
                .reservePrice(BigDecimal.valueOf(100))
                .minimumIncrement(BigDecimal.valueOf(10))
                .currentHighestBid(null)
                .status(SessionStatus.ACTIVE)
                .createdBy(staff)
                .bidCount(0)
                .antiSnipeWindowSeconds(10)
                .antiSnipeExtensionSeconds(30)
                .build();
        session = auctionSessionRepository.save(session);

        // Create 3 user participants for AutoBid
        User u1 = userRepository.save(User.builder().firstName("Robot").lastName("One").email("r1@ex.com").build());
        User u2 = userRepository.save(User.builder().firstName("Robot").lastName("Two").email("r2@ex.com").build());
        User u3 = userRepository.save(User.builder().firstName("Robot").lastName("Three").email("r3@ex.com").build());

        AuctionParticipant p1 = auctionParticipantRepository.save(AuctionParticipant.builder().user(u1).session(session).build());
        AuctionParticipant p2 = auctionParticipantRepository.save(AuctionParticipant.builder().user(u2).session(session).build());
        AuctionParticipant p3 = auctionParticipantRepository.save(AuctionParticipant.builder().user(u3).session(session).build());

        // Save active auto bid configurations
        autoBidConfigRepository.save(AutoBidConfig.builder().participant(p1).maxBidAmount(BigDecimal.valueOf(200)).bidIncrement(BigDecimal.valueOf(10)).isActive(true).build());
        autoBidConfigRepository.save(AutoBidConfig.builder().participant(p2).maxBidAmount(BigDecimal.valueOf(250)).bidIncrement(BigDecimal.valueOf(15)).isActive(true).build());
        autoBidConfigRepository.save(AutoBidConfig.builder().participant(p3).maxBidAmount(BigDecimal.valueOf(300)).bidIncrement(BigDecimal.valueOf(20)).isActive(true).build());
    }

    @Test
    void testConcurrentAutoBids() throws InterruptedException {
        int numThreads = 3;
        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
        CountDownLatch latch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(numThreads);

        for (int i = 0; i < numThreads; i++) {
            executorService.submit(() -> {
                try {
                    latch.await(); // wait for start signal
                    // Trigger auto bid
                    autoBidService.triggerAutoBids(session.getId());
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    finishLatch.countDown();
                }
            });
        }

        // Release the threads at the same time to simulate concurrency
        latch.countDown();

        // Wait for execution to finish
        boolean finished = finishLatch.await(10, TimeUnit.SECONDS);
        assertThat(finished).isTrue();

        // Verify status in DB
        AuctionSession updatedSession = auctionSessionRepository.findById(session.getId()).orElseThrow();
        List<Bid> bids = bidRepository.findAll();

        // Assertions
        System.out.println("--- Concurrency Test Finished ---");
        System.out.println("Final highest bid: " + updatedSession.getCurrentHighestBid());
        System.out.println("Bids placed count: " + bids.size());
        for (Bid b : bids) {
            System.out.println("Bid: User " + b.getParticipant().getUser().getFirstName() + " bid " + b.getAmount());
        }

        // Ensure bids are sequentially valid (each bid must be higher than the previous one)
        bids.sort((b1, b2) -> b1.getAmount().compareTo(b2.getAmount()));
        for (int i = 1; i < bids.size(); i++) {
            assertThat(bids.get(i).getAmount()).isGreaterThan(bids.get(i-1).getAmount());
        }

        // Ensure no two bids have the exact same amount
        long uniqueBidAmountsCount = bids.stream().map(Bid::getAmount).distinct().count();
        assertThat(uniqueBidAmountsCount).isEqualTo(bids.size());
    }
}
