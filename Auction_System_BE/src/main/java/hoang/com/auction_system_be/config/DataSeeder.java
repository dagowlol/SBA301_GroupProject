package hoang.com.auction_system_be.config;

import hoang.com.auction_system_be.entity.*;
import hoang.com.auction_system_be.enums.*;
import hoang.com.auction_system_be.repository.*;
import jakarta.persistence.EntityManager;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DataSeeder implements CommandLineRunner {

    final UserRepository userRepository;
    final CategoryRepository categoryRepository;
    final AuctionItemRepository auctionItemRepository;
    final AuctionSessionRepository auctionSessionRepository;
    final AuctionParticipantRepository auctionParticipantRepository;
    final BidRepository bidRepository;
    final AutoBidConfigRepository autoBidConfigRepository;
    final PaymentRepository paymentRepository;
    final ShippingRepository shippingRepository;
    final DisputeRepository disputeRepository;
    final AuctionExtensionLogRepository auctionExtensionLogRepository;
    final AuditLogRepository auditLogRepository;
    final OtpRepository otpRepository;
    final RefreshTokenRepository refreshTokenRepository;
    final IdempotencyRepository idempotencyRepository;
    final PasswordEncoder passwordEncoder;
    final EntityManager entityManager;

    @Value("${app.seeder.enabled:false}")
    boolean seederEnabled;

    static final String PASSWORD = "password";
    static final Random RANDOM = new Random(301_2026L);

    @Override
    @Transactional
    public void run(String... args) {
        if (!seederEnabled) {
            System.out.println("Data Seeder is disabled.");
            return;
        }

        if (hasExistingData()) {
            System.out.println("Data already exists. Skipping realistic data seeding.");
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        System.out.println("Starting realistic auction data seeding...");

        SeedUsers seedUsers = seedUsers();
        SeedCategories seedCategories = seedCategories();
        List<AuctionItem> items = seedItems(seedUsers, seedCategories, now);
        List<AuctionSession> sessions = seedSessions(items, seedUsers, now);
        Map<AuctionSession, List<AuctionParticipant>> participants = seedParticipants(sessions, seedUsers);
        seedBidsAndWinners(sessions, participants, now);
        seedAutoBidConfigs(sessions, participants);
        seedPaymentsAndShipping(sessions, seedUsers, now);
        seedDisputes(sessions, seedUsers, now);
        seedExtensionLogs(sessions, participants, now);
        seedNews(seedUsers, now);
        seedNotifications(sessions, seedUsers);
        seedAuditLogs(seedUsers, items, sessions);
        seedOperationalRecords(seedUsers, now);

        System.out.println("Realistic data seeding completed: 20 users, 40 items, 15 sessions, 500 bids.");
    }

    private boolean hasExistingData() {
        return userRepository.count() > 0
                || categoryRepository.count() > 0
                || auctionItemRepository.count() > 0
                || auctionSessionRepository.count() > 0
                || bidRepository.count() > 0;
    }

    private SeedUsers seedUsers() {
        List<User> users = new ArrayList<>();

        User admin = user("Admin", "User", "admin@auction.com", RoleName.ADMIN,
                "+84900000001", "District 1, Ho Chi Minh City");
        User manager = user("Auction", "Manager", "manager@auction.com", RoleName.AUCTION_MANAGER,
                "+84900000002", "Ba Dinh District, Hanoi");
        users.add(admin);
        users.add(manager);

        List<User> sellers = List.of(
                user("Linh", "Tran", "seller1@auction.com", RoleName.USER, "+84910000001", "Thao Dien, Thu Duc, Ho Chi Minh City"),
                user("Minh", "Nguyen", "seller2@auction.com", RoleName.USER, "+84910000002", "Hai Chau District, Da Nang"),
                user("An", "Pham", "seller3@auction.com", RoleName.USER, "+84910000003", "Hoan Kiem District, Hanoi"),
                user("Vy", "Le", "seller4@auction.com", RoleName.USER, "+84910000004", "Ninh Kieu District, Can Tho")
        );
        users.addAll(sellers);

        List<User> bidders = new ArrayList<>();
        String[] firstNames = {"Bidder", "Quang", "Hanh", "Khoa", "Mai", "Tuan", "Nhi", "Duc", "Hoa", "Son", "Trang", "Bao", "Lan", "Hieu"};
        String[] lastNames = {"One", "Vo", "Do", "Bui", "Dang", "Ho", "Phan", "Mai", "Ly", "Dao", "Trinh", "Lam", "Vu", "Ngo"};
        for (int i = 0; i < 14; i++) {
            bidders.add(user(firstNames[i], lastNames[i], "bidder" + (i + 1) + "@auction.com",
                    RoleName.USER, "+8492" + String.format("%07d", i + 1),
                    "Receiver address " + (i + 1) + ", Vietnam"));
        }
        users.addAll(bidders);

        userRepository.saveAll(users);
        return new SeedUsers(admin, manager, sellers, bidders, users);
    }

    private User user(String firstName, String lastName, String email, RoleName role, String phone, String address) {
        return User.builder()
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .passwordHash(passwordEncoder.encode(PASSWORD))
                .phoneNumber(phone)
                .address(address)
                .role(role)
                .status(UserStatus.ACTIVE)
                .authProvider(AuthProvider.LOCAL)
                .failedLoginAttempts(0)
                .build();
    }

    private SeedCategories seedCategories() {
        Category fineArt = category("Fine Art", "Original paintings, limited prints, and collectible artworks.", null, 1);
        Category decorativeArt = category("Decorative Art", "Ceramics, porcelain, glass, and design objects.", null, 2);
        Category jewelry = category("Jewelry & Watches", "Precious jewelry, vintage watches, and luxury accessories.", null, 3);
        Category antiques = category("Antiques", "Rare objects, furniture, maps, books, and historic collectibles.", null, 4);
        Category asianArt = category("Asian Art", "Vietnamese, Chinese, Japanese, and Southeast Asian works.", null, 5);
        Category photography = category("Photography", "Signed photographic prints and archival editions.", null, 6);

        List<Category> parents = categoryRepository.saveAll(List.of(fineArt, decorativeArt, jewelry, antiques, asianArt, photography));

        Category paintings = category("Paintings", "Oil, acrylic, lacquer, and watercolor paintings.", fineArt, 11);
        Category sculpture = category("Sculpture", "Bronze, marble, wood, and mixed-media sculpture.", fineArt, 12);
        Category ceramics = category("Ceramics & Porcelain", "Functional and decorative ceramics.", decorativeArt, 21);
        Category watches = category("Watches", "Mechanical and quartz watches.", jewelry, 31);
        Category furniture = category("Furniture", "Cabinets, tables, chairs, and design furniture.", antiques, 41);
        Category books = category("Rare Books & Maps", "First editions, manuscripts, maps, and documents.", antiques, 42);
        Category vietnameseArt = category("Vietnamese Art", "Vietnamese modern and contemporary art.", asianArt, 51);
        Category prints = category("Prints & Multiples", "Lithographs, etchings, screenprints, and multiples.", photography, 61);

        List<Category> children = categoryRepository.saveAll(List.of(paintings, sculpture, ceramics, watches, furniture, books, vietnameseArt, prints));
        return new SeedCategories(parents, children);
    }

    private Category category(String name, String description, Category parent, int sortOrder) {
        return Category.builder()
                .name(name)
                .description(description)
                .parentCategory(parent)
                .sortOrder(sortOrder)
                .build();
    }

    private List<AuctionItem> seedItems(SeedUsers users, SeedCategories categories, LocalDateTime now) {
        Map<String, Category> categoryByName = new HashMap<>();
        categories.all().forEach(category -> categoryByName.put(category.getName(), category));

        List<ItemSeed> seeds = itemSeeds();
        List<AuctionItem> items = new ArrayList<>();
        for (int i = 0; i < seeds.size(); i++) {
            ItemSeed seed = seeds.get(i);
            User seller = users.sellers().get(i % users.sellers().size());
            AuctionItem item = AuctionItem.builder()
                    .name(seed.name())
                    .description(seed.description())
                    .category(categoryByName.get(seed.category()))
                    .seller(seller)
                    .startingPrice(money(seed.startingPrice()))
                    .reservePrice(money(seed.reservePrice()))
                    .status(seed.status())
                    .reviewedBy(seed.status() == ItemStatus.PENDING ? null : users.manager())
                    .reviewedAt(seed.status() == ItemStatus.PENDING ? null : now.minusDays(40L - (i % 30)))
                    .rejectionReason(seed.status() == ItemStatus.REJECTED ? seed.rejectionReason() : null)
                    .build();

            item.getImages().add(ItemImage.builder()
                    .item(item)
                    .imageUrl(seed.primaryImage())
                    .isPrimary(true)
                    .sortOrder(1)
                    .build());
            item.getImages().add(ItemImage.builder()
                    .item(item)
                    .imageUrl(seed.secondaryImage())
                    .isPrimary(false)
                    .sortOrder(2)
                    .build());
            items.add(item);
        }

        return auctionItemRepository.saveAll(items);
    }

    private List<AuctionSession> seedSessions(List<AuctionItem> items, SeedUsers users, LocalDateTime now) {
        SessionSeed[] seeds = {
                new SessionSeed(0, SessionStatus.ENDED, now.minusDays(120), now.minusDays(119).plusHours(3), 55),
                new SessionSeed(1, SessionStatus.ENDED, now.minusDays(92), now.minusDays(91).plusHours(4), 48),
                new SessionSeed(2, SessionStatus.ENDED, now.minusDays(63), now.minusDays(62).plusHours(5), 62),
                new SessionSeed(3, SessionStatus.ENDED, now.minusDays(41), now.minusDays(40).plusHours(2), 35),
                new SessionSeed(4, SessionStatus.ENDED, now.minusDays(23), now.minusDays(22).plusHours(6), 41),
                new SessionSeed(5, SessionStatus.ENDED, now.minusDays(11), now.minusDays(10).plusHours(3), 50),
                new SessionSeed(6, SessionStatus.RESERVE_NOT_MET, now.minusDays(18), now.minusDays(17).plusHours(2), 32),
                new SessionSeed(7, SessionStatus.CANCELLED, now.minusDays(7), now.minusDays(6), 12),
                new SessionSeed(8, SessionStatus.ACTIVE, now.minusHours(8), now.plusHours(16), 50),
                new SessionSeed(9, SessionStatus.ACTIVE, now.minusHours(5), now.plusHours(20), 45),
                new SessionSeed(10, SessionStatus.ACTIVE, now.minusHours(2), now.plusDays(1), 40),
                new SessionSeed(11, SessionStatus.ACTIVE, now.minusHours(1), now.plusDays(2), 30),
                new SessionSeed(12, SessionStatus.SCHEDULED, now.plusDays(2), now.plusDays(3), 0),
                new SessionSeed(13, SessionStatus.SCHEDULED, now.plusDays(4), now.plusDays(5), 0),
                new SessionSeed(14, SessionStatus.SCHEDULED, now.plusDays(7), now.plusDays(8), 0)
        };

        List<AuctionSession> sessions = new ArrayList<>();
        for (SessionSeed seed : seeds) {
            AuctionItem item = items.get(seed.itemIndex());
            BigDecimal reserve = item.getReservePrice();
            BigDecimal increment = chooseIncrement(reserve);
            AuctionSession session = AuctionSession.builder()
                    .item(item)
                    .startTime(seed.startTime())
                    .endTime(seed.endTime())
                    .reservePrice(reserve)
                    .minimumIncrement(increment)
                    .currentHighestBid(item.getStartingPrice())
                    .status(seed.status())
                    .createdBy(users.manager())
                    .antiSnipeWindowSeconds(30)
                    .antiSnipeExtensionSeconds(120)
                    .bidCount(seed.bidCount())
                    .cancellationReason(seed.status() == SessionStatus.CANCELLED
                            ? "Seller requested postponement after authenticity paperwork was delayed."
                            : null)
                    .build();
            item.setStatus(statusForSession(seed.status()));
            sessions.add(session);
        }

        auctionItemRepository.saveAll(items.subList(0, 15));
        return auctionSessionRepository.saveAll(sessions);
    }

    private ItemStatus statusForSession(SessionStatus sessionStatus) {
        return switch (sessionStatus) {
            case ACTIVE -> ItemStatus.ACTIVE;
            case ENDED -> ItemStatus.SOLD;
            case PAID -> ItemStatus.PAID;
            case SCHEDULED, RESERVE_NOT_MET, CANCELLED, POSTPONED -> ItemStatus.APPROVED;
        };
    }

    private BigDecimal chooseIncrement(BigDecimal reserve) {
        if (reserve.compareTo(money(100_000_000)) >= 0) {
            return money(2_000_000);
        }
        if (reserve.compareTo(money(30_000_000)) >= 0) {
            return money(1_000_000);
        }
        return money(500_000);
    }

    private Map<AuctionSession, List<AuctionParticipant>> seedParticipants(List<AuctionSession> sessions, SeedUsers users) {
        Map<AuctionSession, List<AuctionParticipant>> bySession = new LinkedHashMap<>();
        List<AuctionParticipant> allParticipants = new ArrayList<>();
        int bidderSize = users.bidders().size();
        for (int i = 0; i < sessions.size(); i++) {
            AuctionSession session = sessions.get(i);
            int participantCount = session.getStatus() == SessionStatus.SCHEDULED ? 6 : 10 + (i % 4);
            participantCount = Math.min(participantCount, bidderSize);
            List<AuctionParticipant> sessionParticipants = new ArrayList<>();
            Set<Long> addedUserIds = new HashSet<>();
            int idx = i;
            for (int j = 0; j < participantCount; j++) {
                while (addedUserIds.contains(users.bidders().get(idx % bidderSize).getId())) {
                    idx++;
                }
                User bidder = users.bidders().get(idx % bidderSize);
                addedUserIds.add(bidder.getId());
                AuctionParticipant participant = AuctionParticipant.builder()
                        .session(session)
                        .user(bidder)
                        .build();
                sessionParticipants.add(participant);
                allParticipants.add(participant);
                idx++;
            }
            bySession.put(session, sessionParticipants);
        }
        auctionParticipantRepository.saveAll(allParticipants);
        return bySession;
    }

    private void seedBidsAndWinners(List<AuctionSession> sessions, Map<AuctionSession, List<AuctionParticipant>> participants, LocalDateTime now) {
        List<Bid> bids = new ArrayList<>();
        for (AuctionSession session : sessions) {
            int bidCount = session.getBidCount();
            if (bidCount <= 0) {
                continue;
            }

            List<AuctionParticipant> sessionParticipants = participants.get(session);
            BigDecimal amount = session.getItem().getStartingPrice();
            BigDecimal increment = session.getMinimumIncrement();
            LocalDateTime firstBidAt = session.getStartTime().plusMinutes(8);
            AuctionParticipant winner = null;

            for (int i = 0; i < bidCount; i++) {
                AuctionParticipant participant = sessionParticipants.get((i * 3 + session.getId().intValue()) % sessionParticipants.size());
                BigDecimal step = increment.multiply(BigDecimal.valueOf(1 + RANDOM.nextInt(3)));
                amount = amount.add(step);
                if (session.getStatus() == SessionStatus.RESERVE_NOT_MET
                        && amount.compareTo(session.getReservePrice().subtract(increment)) >= 0) {
                    amount = session.getReservePrice().subtract(increment);
                }
                winner = participant;

                BidStatus status = BidStatus.OUTBID;
                if (i == bidCount - 1) {
                    status = session.getStatus() == SessionStatus.ENDED ? BidStatus.WON : BidStatus.ACTIVE;
                }
                if (session.getStatus() == SessionStatus.CANCELLED && i >= bidCount - 2) {
                    status = BidStatus.CANCELLED;
                }

                bids.add(Bid.builder()
                        .participant(participant)
                        .amount(amount)
                        .isAutoBid(i % 7 == 0)
                        .status(status)
                        .bidTimestamp(firstBidAt.plusMinutes(i * 3L))
                        .ipAddress("113.161." + (20 + (i % 80)) + "." + (10 + (i % 180)))
                        .isSuspicious(i % 53 == 0)
                        .build());
            }

            session.setCurrentHighestBid(amount);
            if (session.getStatus() == SessionStatus.ENDED) {
                session.setCurrentWinnerParticipant(winner);
            }
            if (session.getStatus() == SessionStatus.ACTIVE) {
                session.setCurrentWinnerParticipant(winner);
            }
            if (session.getStatus() == SessionStatus.RESERVE_NOT_MET || session.getStatus() == SessionStatus.CANCELLED) {
                session.setCurrentWinnerParticipant(null);
            }
        }

        bidRepository.saveAll(bids);
        auctionSessionRepository.saveAll(sessions);
    }

    private void seedAutoBidConfigs(List<AuctionSession> sessions, Map<AuctionSession, List<AuctionParticipant>> participants) {
        List<AutoBidConfig> configs = new ArrayList<>();
        for (AuctionSession session : sessions) {
            if (session.getStatus() != SessionStatus.ACTIVE) {
                continue;
            }
            List<AuctionParticipant> sessionParticipants = participants.get(session);
            for (int i = 0; i < Math.min(3, sessionParticipants.size()); i++) {
                AuctionParticipant participant = sessionParticipants.get(i);
                configs.add(AutoBidConfig.builder()
                        .participant(participant)
                        .maxBidAmount(session.getCurrentHighestBid().add(session.getMinimumIncrement().multiply(BigDecimal.valueOf(20L + i * 5L))))
                        .bidIncrement(session.getMinimumIncrement())
                        .isActive(i != 2)
                        .build());
            }
        }
        autoBidConfigRepository.saveAll(configs);
    }

    private void seedPaymentsAndShipping(List<AuctionSession> sessions, SeedUsers users, LocalDateTime now) {
        List<Payment> payments = new ArrayList<>();
        List<Shipping> shippings = new ArrayList<>();
        int paidIndex = 1;

        for (AuctionSession session : sessions) {
            AuctionParticipant winner = session.getCurrentWinnerParticipant();
            if (session.getStatus() != SessionStatus.ENDED || winner == null) {
                continue;
            }

            PaymentStatus paymentStatus = switch (paidIndex) {
                case 1, 2, 3, 4 -> PaymentStatus.PAID;
                case 5 -> PaymentStatus.PENDING;
                default -> PaymentStatus.FAILED;
            };
            LocalDateTime paidAt = paymentStatus == PaymentStatus.PAID ? session.getEndTime().plusHours(2 + paidIndex) : null;
            payments.add(Payment.builder()
                    .participant(winner)
                    .type(PaymentType.FINAL_PAYMENT)
                    .amount(session.getCurrentHighestBid())
                    .status(paymentStatus)
                    .transactionId("FINAL-" + session.getId() + "-" + paidIndex)
                    .paymentGatewayRef(paymentStatus == PaymentStatus.PAID ? "VNP-" + now.getYear() + "-000" + paidIndex : null)
                    .paymentMethod(paymentStatus == PaymentStatus.PAID ? "VNPAY" : null)
                    .paidAt(paidAt)
                    .failureReason(paymentStatus == PaymentStatus.FAILED ? "Card issuer declined the transaction." : null)
                    .build());

            if (paymentStatus == PaymentStatus.PAID) {
                ShippingStatus shippingStatus = switch (paidIndex) {
                    case 1 -> ShippingStatus.DELIVERED;
                    case 2 -> ShippingStatus.IN_TRANSIT;
                    case 3 -> ShippingStatus.PENDING;
                    default -> ShippingStatus.RETURNED;
                };
                AuctionItem item = session.getItem();
                item.setStatus(switch (shippingStatus) {
                    case DELIVERED -> ItemStatus.DELIVERED;
                    case IN_TRANSIT -> ItemStatus.SHIPPING;
                    case RETURNED -> ItemStatus.SOLD;
                    case PENDING, CANCELLED -> ItemStatus.PAID;
                });
                shippings.add(Shipping.builder()
                        .item(item)
                        .buyer(winner.getUser())
                        .trackingNumber("GHN" + now.getYear() + String.format("%06d", paidIndex))
                        .carrier(paidIndex % 2 == 0 ? "Giao Hang Nhanh" : "Viettel Post")
                        .shippingFee(money(85_000 + paidIndex * 15_000L))
                        .shippingAddress(winner.getUser().getAddress())
                        .receiverName(winner.getUser().getFirstName() + " " + winner.getUser().getLastName())
                        .receiverPhone(winner.getUser().getPhoneNumber())
                        .status(shippingStatus)
                        .build());
            }
            paidIndex++;
        }

        for (AuctionSession session : sessions) {
            if (session.getStatus() != SessionStatus.ACTIVE) {
                continue;
            }
            List<AuctionParticipant> activeParticipants = auctionParticipantRepository.findAll().stream()
                    .filter(p -> p.getSession().getId().equals(session.getId()))
                    .limit(3)
                    .toList();
            for (AuctionParticipant participant : activeParticipants) {
                payments.add(Payment.builder()
                        .participant(participant)
                        .type(PaymentType.DEPOSIT)
                        .amount(money(1_000_000))
                        .status(PaymentStatus.PAID)
                        .transactionId("DEP-" + session.getId() + "-" + participant.getId())
                        .paymentGatewayRef("DEP-GW-" + session.getId() + "-" + participant.getId())
                        .paymentMethod("VNPAY")
                        .paidAt(session.getStartTime().minusHours(2))
                        .build());
            }
        }

        paymentRepository.saveAll(payments);
        auctionItemRepository.saveAll(sessions.stream().map(AuctionSession::getItem).toList());
        shippingRepository.saveAll(shippings);
    }

    private void seedDisputes(List<AuctionSession> sessions, SeedUsers users, LocalDateTime now) {
        List<Dispute> disputes = new ArrayList<>();
        String[] types = {"Item Mismatch", "Payment Issue", "Shipping Delay", "Authenticity Review", "Seller Response"};
        for (int i = 0; i < 12; i++) {
            AuctionSession session = sessions.get(i % 7);
            DisputeStatus status = switch (i % 4) {
                case 0 -> DisputeStatus.OPEN;
                case 1 -> DisputeStatus.IN_REVIEW;
                case 2 -> DisputeStatus.RESOLVED;
                default -> DisputeStatus.REJECTED;
            };
            disputes.add(Dispute.builder()
                    .session(session)
                    .raisedBy(users.bidders().get((i * 2) % users.bidders().size()))
                    .type(types[i % types.length])
                    .description("Customer support case created after the auction closed. Evidence and messages were attached for manager review.")
                    .status(status)
                    .assignedManager(users.manager())
                    .resolution(status == DisputeStatus.RESOLVED
                            ? "Reviewed listing photos, seller documents, and delivery evidence. Case resolved in buyer's favor."
                            : status == DisputeStatus.REJECTED
                            ? "Evidence did not support the claim. Dispute rejected after manager review."
                            : null)
                    .resolvedAt(status == DisputeStatus.RESOLVED || status == DisputeStatus.REJECTED ? now.minusDays(5L + i) : null)
                    .build());
        }
        disputeRepository.saveAll(disputes);
    }

    private void seedExtensionLogs(List<AuctionSession> sessions, Map<AuctionSession, List<AuctionParticipant>> participants, LocalDateTime now) {
        List<AuctionExtensionLog> logs = new ArrayList<>();
        for (AuctionSession session : sessions) {
            if (session.getStatus() != SessionStatus.ACTIVE && session.getStatus() != SessionStatus.ENDED) {
                continue;
            }
            LocalDateTime oldEnd = session.getEndTime().minusMinutes(2);
            logs.add(AuctionExtensionLog.builder()
                    .session(session)
                    .triggeredByParticipant(participants.get(session).get(0))
                    .windowSeconds(session.getAntiSnipeWindowSeconds())
                    .extensionSeconds(session.getAntiSnipeExtensionSeconds())
                    .oldEndTime(oldEnd)
                    .newEndTime(session.getEndTime())
                    .reason("Bid placed inside anti-snipe window.")
                    .build());
        }
        auctionExtensionLogRepository.saveAll(logs);
    }

    private void seedNews(SeedUsers users, LocalDateTime now) {
        List<News> news = List.of(
                news("July Modern Art Evening Sale closes above estimate", NewsType.NEWS, true, "https://images.unsplash.com/photo-1541961017774-22349e4a1262?w=1200&auto=format&fit=crop&q=80", users.manager(), now.minusDays(2)),
                news("How absentee bidding works on Annexe Auction", NewsType.ANNOUNCEMENT, false, "https://images.unsplash.com/photo-1516321318423-f06f85e504b3?w=1200&auto=format&fit=crop&q=80", users.manager(), now.minusDays(9)),
                news("Important guide to object condition reports", NewsType.NEWS, false, "https://images.unsplash.com/photo-1450101499163-c8848c66ca85?w=1200&auto=format&fit=crop&q=80", users.admin(), now.minusDays(18)),
                news("Vietnamese lacquer collection preview", NewsType.FEATURED_EVENT, true, "https://images.unsplash.com/photo-1579783902614-a3fb3927b6a5?w=1200&auto=format&fit=crop&q=80", users.manager(), now.plusDays(3)),
                news("Shipping schedule during national holidays", NewsType.ANNOUNCEMENT, false, "https://images.unsplash.com/photo-1566576721346-d4a3b4eaeb55?w=1200&auto=format&fit=crop&q=80", users.admin(), now.minusDays(30)),
                news("Collector spotlight: building a ceramics collection", NewsType.NEWS, false, "https://images.unsplash.com/photo-1612196808214-b8e1d6145a8c?w=1200&auto=format&fit=crop&q=80", users.manager(), now.minusDays(45)),
                news("Auction manager notes on suspicious bidding", NewsType.NEWS, false, "https://images.unsplash.com/photo-1554224155-6726b3ff858f?w=1200&auto=format&fit=crop&q=80", users.admin(), now.minusDays(57)),
                news("Upcoming watches and jewelry auction", NewsType.FEATURED_EVENT, true, "https://images.unsplash.com/photo-1524592094714-0f0654e20314?w=1200&auto=format&fit=crop&q=80", users.manager(), now.plusDays(10))
        );
        news.forEach(entityManager::persist);
    }

    private News news(String title, NewsType type, boolean featured, String image, User author, LocalDateTime publishedAt) {
        return News.builder()
                .title(title)
                .content("Editorial content prepared by the auction team with sale details, buyer guidance, and operational notes.")
                .thumbnailUrl(image)
                .type(type)
                .status(NewsStatus.ENABLED)
                .isFeatured(featured)
                .author(author)
                .publishedAt(publishedAt)
                .build();
    }

    private void seedNotifications(List<AuctionSession> sessions, SeedUsers users) {
        for (int i = 0; i < 28; i++) {
            User user = users.bidders().get(i % users.bidders().size());
            AuctionSession session = sessions.get(i % sessions.size());
            NotificationType type = NotificationType.values()[i % NotificationType.values().length];
            entityManager.persist(Notification.builder()
                    .user(user)
                    .type(type)
                    .title(notificationTitle(type))
                    .message("System notification generated from realistic seed data for session #" + session.getId() + ".")
                    .isRead(i % 3 == 0)
                    .referenceId(session.getId())
                    .referenceType("AUCTION_SESSION")
                    .build());
        }
    }

    private String notificationTitle(NotificationType type) {
        return switch (type) {
            case OUTBID -> "You have been outbid";
            case AUCTION_REMINDER -> "Auction starts soon";
            case WINNER -> "You won an auction";
            case PAYMENT_SUCCESS -> "Payment confirmed";
            case PAYMENT_FAILED -> "Payment failed";
        };
    }

    private void seedAuditLogs(SeedUsers users, List<AuctionItem> items, List<AuctionSession> sessions) {
        List<AuditLog> logs = new ArrayList<>();
        for (int i = 0; i < 32; i++) {
            boolean itemLog = i % 2 == 0;
            logs.add(AuditLog.builder()
                    .user(i % 5 == 0 ? users.admin() : users.manager())
                    .action(itemLog ? "ITEM_REVIEWED" : "SESSION_UPDATED")
                    .entityType(itemLog ? "AuctionItem" : "AuctionSession")
                    .entityId(itemLog ? items.get(i % items.size()).getId() : sessions.get(i % sessions.size()).getId())
                    .details(itemLog
                            ? "Reviewed item metadata, images, reserve price, and seller profile."
                            : "Updated schedule/status information during auction operations.")
                    .ipAddress("10.10.0." + (20 + i))
                    .build());
        }
        auditLogRepository.saveAll(logs);
    }

    private void seedOperationalRecords(SeedUsers users, LocalDateTime now) {
        otpRepository.saveAll(List.of(
                OtpRecord.builder().email("admin@auction.com").otpCode("111111").expiryTime(now.minusDays(2)).used(true).build(),
                OtpRecord.builder().email("manager@auction.com").otpCode("222222").expiryTime(now.minusDays(1)).used(true).build(),
                OtpRecord.builder().email("bidder1@auction.com").otpCode("333333").expiryTime(now.minusHours(12)).used(true).build()
        ));

        refreshTokenRepository.saveAll(List.of(
                RefreshToken.builder().token("seed-revoked-refresh-admin").userEmail("admin@auction.com").expiryTime(Instant.now().minusSeconds(3600)).revoked(true).build(),
                RefreshToken.builder().token("seed-revoked-refresh-manager").userEmail("manager@auction.com").expiryTime(Instant.now().minusSeconds(7200)).revoked(true).build()
        ));

        idempotencyRepository.saveAll(List.of(
                IdempotencyRecord.builder().idempotencyKey("seed-idempotency-key-000000000000000000000000000001").statusCode(201).responseBody("{\"message\":\"seeded\"}").userId(users.manager().getId()).expiresAt(now.minusDays(1)).build(),
                IdempotencyRecord.builder().idempotencyKey("seed-idempotency-key-000000000000000000000000000002").statusCode(409).responseBody("{\"message\":\"duplicate\"}").userId(users.admin().getId()).expiresAt(now.minusDays(1)).build()
        ));
    }

    private List<ItemSeed> itemSeeds() {
        List<String> images = List.of(
                "https://images.unsplash.com/photo-1579783902614-a3fb3927b6a5?w=1200&auto=format&fit=crop&q=80",
                "https://images.unsplash.com/photo-1541961017774-22349e4a1262?w=1200&auto=format&fit=crop&q=80",
                "https://images.unsplash.com/photo-1578301978693-85fa9c0320b9?w=1200&auto=format&fit=crop&q=80",
                "https://images.unsplash.com/photo-1579783900882-c0d3dad7b119?w=1200&auto=format&fit=crop&q=80",
                "https://images.unsplash.com/photo-1580136579312-94651dfd596d?w=1200&auto=format&fit=crop&q=80",
                "https://images.unsplash.com/photo-1605721911519-3dfeb3be25e7?w=1200&auto=format&fit=crop&q=80",
                "https://images.unsplash.com/photo-1612196808214-b8e1d6145a8c?w=1200&auto=format&fit=crop&q=80",
                "https://images.unsplash.com/photo-1524592094714-0f0654e20314?w=1200&auto=format&fit=crop&q=80",
                "https://images.unsplash.com/photo-1515562141207-7a88fb7ce338?w=1200&auto=format&fit=crop&q=80",
                "https://images.unsplash.com/photo-1579783902614-a3fb3927b6a5?w=1200&auto=format&fit=crop&q=80",
                "https://images.unsplash.com/photo-1513519245088-0e12902e5a38?w=1200&auto=format&fit=crop&q=80",
                "https://images.unsplash.com/photo-1598300042247-d088f8ab3a91?w=1200&auto=format&fit=crop&q=80",
                "https://images.unsplash.com/photo-1460661419201-fd4cecdf8a8b?w=1200&auto=format&fit=crop&q=80",
                "https://images.unsplash.com/photo-1515405295579-ba7b45403062?w=1200&auto=format&fit=crop&q=80",
                "https://images.unsplash.com/photo-1531913764164-f85c52e6e654?w=1200&auto=format&fit=crop&q=80",
                "https://images.unsplash.com/photo-1519681393784-d120267933ba?w=1200&auto=format&fit=crop&q=80"
        );

        return List.of(
                item("Lotus Pond Lacquer Panel", "Vietnamese Art", 18_000_000, 32_000_000, ItemStatus.APPROVED, images, 0),
                item("Saigon River Oil Painting", "Paintings", 12_000_000, 25_000_000, ItemStatus.APPROVED, images, 1),
                item("Bronze Bodhisattva Figure", "Sculpture", 35_000_000, 70_000_000, ItemStatus.APPROVED, images, 5),
                item("Blue and White Porcelain Jar", "Ceramics & Porcelain", 9_000_000, 18_000_000, ItemStatus.APPROVED, images, 6),
                item("Vintage Omega Seamaster", "Watches", 28_000_000, 55_000_000, ItemStatus.APPROVED, images, 7),
                item("Art Deco Diamond Brooch", "Jewelry & Watches", 45_000_000, 90_000_000, ItemStatus.APPROVED, images, 8),
                item("French Walnut Writing Desk", "Furniture", 22_000_000, 42_000_000, ItemStatus.APPROVED, images, 10),
                item("Hand-Colored Indochina Map", "Rare Books & Maps", 8_000_000, 16_000_000, ItemStatus.APPROVED, images, 13),
                item("Contemporary Abstract Canvas", "Paintings", 15_000_000, 30_000_000, ItemStatus.APPROVED, images, 2),
                item("Signed Silver Gelatin Print", "Photography", 7_500_000, 14_000_000, ItemStatus.APPROVED, images, 15),
                item("Carved Rosewood Cabinet", "Furniture", 26_000_000, 52_000_000, ItemStatus.APPROVED, images, 10),
                item("Ming Style Ceramic Bowl", "Ceramics & Porcelain", 11_000_000, 24_000_000, ItemStatus.APPROVED, images, 6),
                item("Limited Edition Screenprint", "Prints & Multiples", 5_000_000, 10_000_000, ItemStatus.APPROVED, images, 12),
                item("Jade Pendant Necklace", "Jewelry & Watches", 16_000_000, 31_000_000, ItemStatus.APPROVED, images, 9),
                item("Marble Torso Study", "Sculpture", 20_000_000, 38_000_000, ItemStatus.APPROVED, images, 5),
                item("Lacquer Landscape Triptych", "Vietnamese Art", 24_000_000, 48_000_000, ItemStatus.PENDING, images, 0),
                item("Antique Travel Trunk", "Antiques", 6_000_000, 12_000_000, ItemStatus.PENDING, images, 10),
                item("Collector Fountain Pen Set", "Antiques", 4_000_000, 8_000_000, ItemStatus.PENDING, images, 14),
                item("Gold Hoop Earrings", "Jewelry & Watches", 10_000_000, 20_000_000, ItemStatus.PENDING, images, 8),
                item("Studio Pottery Vase", "Decorative Art", 3_500_000, 7_000_000, ItemStatus.PENDING, images, 6),
                item("Northern Highlands Photograph", "Photography", 4_500_000, 9_000_000, ItemStatus.REJECTED, images, 15),
                item("Unsigned Landscape Sketch", "Fine Art", 2_000_000, 5_000_000, ItemStatus.REJECTED, images, 3),
                item("Mother of Pearl Box", "Decorative Art", 6_500_000, 13_000_000, ItemStatus.APPROVED, images, 11),
                item("First Edition Poetry Book", "Rare Books & Maps", 7_000_000, 15_000_000, ItemStatus.APPROVED, images, 13),
                item("Modernist Bronze Horse", "Sculpture", 18_500_000, 37_000_000, ItemStatus.APPROVED, images, 5),
                item("Silk Road Textile Fragment", "Asian Art", 13_000_000, 26_000_000, ItemStatus.APPROVED, images, 11),
                item("Moonlit Pagoda Watercolor", "Paintings", 9_500_000, 19_000_000, ItemStatus.APPROVED, images, 4),
                item("Vintage Longines Dress Watch", "Watches", 24_000_000, 46_000_000, ItemStatus.APPROVED, images, 7),
                item("Pearl Strand Necklace", "Jewelry & Watches", 21_000_000, 40_000_000, ItemStatus.APPROVED, images, 8),
                item("Japanese Woodblock Print", "Prints & Multiples", 12_000_000, 24_000_000, ItemStatus.APPROVED, images, 12),
                item("Colonial Teak Armchair", "Furniture", 14_000_000, 28_000_000, ItemStatus.APPROVED, images, 10),
                item("Celadon Glazed Charger", "Ceramics & Porcelain", 8_500_000, 17_000_000, ItemStatus.APPROVED, images, 6),
                item("Archive Fashion Photograph", "Photography", 6_000_000, 12_000_000, ItemStatus.APPROVED, images, 15),
                item("Gold Signet Ring", "Jewelry & Watches", 13_000_000, 25_000_000, ItemStatus.APPROVED, images, 9),
                item("Scholar's Rock on Stand", "Asian Art", 17_000_000, 34_000_000, ItemStatus.APPROVED, images, 11),
                item("Large Abstract Lacquer Work", "Vietnamese Art", 30_000_000, 60_000_000, ItemStatus.APPROVED, images, 0),
                item("Handwritten Music Manuscript", "Rare Books & Maps", 5_500_000, 11_000_000, ItemStatus.APPROVED, images, 13),
                item("Cut Glass Decanter Pair", "Decorative Art", 4_800_000, 9_500_000, ItemStatus.APPROVED, images, 6),
                item("Small Bronze Temple Bell", "Antiques", 7_200_000, 14_500_000, ItemStatus.APPROVED, images, 5),
                item("Signed Contemporary Print", "Prints & Multiples", 4_200_000, 8_500_000, ItemStatus.APPROVED, images, 12)
        );
    }

    private ItemSeed item(String name, String category, long start, long reserve, ItemStatus status, List<String> images, int imageIndex) {
        return new ItemSeed(
                name,
                category,
                "Professionally catalogued lot with condition report, provenance notes, and high-resolution public reference images.",
                start,
                reserve,
                status,
                images.get(imageIndex % images.size()),
                images.get((imageIndex + 3) % images.size()),
                status == ItemStatus.REJECTED ? "Insufficient provenance documents for public auction approval." : null
        );
    }

    private BigDecimal money(long value) {
        return BigDecimal.valueOf(value).setScale(2);
    }

    private record SeedUsers(User admin, User manager, List<User> sellers, List<User> bidders, List<User> all) {
    }

    private record SeedCategories(List<Category> parents, List<Category> children) {
        List<Category> all() {
            List<Category> all = new ArrayList<>(parents);
            all.addAll(children);
            return all;
        }
    }

    private record ItemSeed(
            String name,
            String category,
            String description,
            long startingPrice,
            long reservePrice,
            ItemStatus status,
            String primaryImage,
            String secondaryImage,
            String rejectionReason) {
    }

    private record SessionSeed(int itemIndex, SessionStatus status, LocalDateTime startTime, LocalDateTime endTime, int bidCount) {
    }
}
