package hoang.com.auction_system_be.service.impl;

import hoang.com.auction_system_be.dto.request.AuctionSessionRequest;
import hoang.com.auction_system_be.dto.request.AuctionSessionUpdateRequest;
import hoang.com.auction_system_be.dto.request.PlaceBidRequest;
import hoang.com.auction_system_be.dto.response.*;
import hoang.com.auction_system_be.entity.*;
import hoang.com.auction_system_be.enums.ItemStatus;
import hoang.com.auction_system_be.enums.SessionStatus;
import hoang.com.auction_system_be.exception.AppException;
import hoang.com.auction_system_be.exception.ErrorCode;
import hoang.com.auction_system_be.mapper.AuctionSessionMapper;
import hoang.com.auction_system_be.mapper.BidMapper;
import hoang.com.auction_system_be.repository.*;
import hoang.com.auction_system_be.service.DistributedLockService;
import hoang.com.auction_system_be.service.auth.SecurityContextService;
import hoang.com.auction_system_be.service.session.AuctionSessionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import hoang.com.auction_system_be.event.AuditLogEvent;
import hoang.com.auction_system_be.event.AuctionSessionCreatedEvent;
import hoang.com.auction_system_be.event.BidPlacedEvent;
import hoang.com.auction_system_be.event.SessionExtendedEvent;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuctionSessionServiceImplTest {

    @Mock
    AuctionSessionRepository auctionSessionRepository;

    @Mock
    BidRepository bidRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    AuctionItemRepository auctionItemRepository;

    @Mock
    AuctionParticipantRepository auctionParticipantRepository;

    @Mock
    AuctionExtensionLogRepository auctionExtensionLogRepository;

    @Mock
    SecurityContextService authenticationService;

    @Mock
    ApplicationEventPublisher eventPublisher;

    @Mock
    TransactionTemplate transactionTemplate;

    @Mock
    DistributedLockService lockService;

    @Spy
    AuctionSessionMapper auctionSessionMapper = new AuctionSessionMapper();

    @Spy
    BidMapper bidMapper = new BidMapper();

    @InjectMocks
    AuctionSessionServiceImpl auctionSessionService;

    User testUser;
    AuctionItem testItem;
    AuctionSession testSession;
    AuctionParticipant testParticipant;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .build();

        ItemImage primaryImage = ItemImage.builder()
                .id(1L)
                .imageUrl("http://example.com/image1.jpg")
                .isPrimary(true)
                .build();

        testItem = AuctionItem.builder()
                .id(1L)
                .name("Test Item")
                .description("Test Description")
                .startingPrice(BigDecimal.valueOf(100))
                .status(ItemStatus.APPROVED)
                .images(List.of(primaryImage))
                .build();

        testSession = AuctionSession.builder()
                .id(1L)
                .item(testItem)
                .status(SessionStatus.ACTIVE)
                .currentHighestBid(BigDecimal.valueOf(100))
                .minimumIncrement(BigDecimal.valueOf(10))
                .startTime(LocalDateTime.now().minusHours(1))
                .endTime(LocalDateTime.now().plusHours(1))
                .bidCount(0)
                .antiSnipeWindowSeconds(10)
                .antiSnipeExtensionSeconds(30)
                .createdBy(testUser)
                .participants(new ArrayList<>())
                .build();

        testParticipant = AuctionParticipant.builder()
                .id(1L)
                .user(testUser)
                .session(testSession)
                .build();
    }

    // ─── 1. getAuctionSessionDetail Tests ─────────────────────────────────

    @Test
    @DisplayName("getAuctionSessionDetail - success")
    void getAuctionSessionDetail_success() {
        // Arrange
        testSession.setCurrentWinnerParticipant(testParticipant);
        Bid recentBid = Bid.builder()
                .id(1L)
                .bidAmount(BigDecimal.valueOf(100))
                .bidTimestamp(LocalDateTime.now())
                .participant(testParticipant)
                .build();

        when(auctionSessionRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(testSession));
        when(bidRepository.findTop10ByParticipantSessionIdOrderByBidTimestampDesc(1L))
                .thenReturn(List.of(recentBid));

        // Act
        AuctionSessionDetailResponse response = auctionSessionService.getAuctionSessionDetail(1L);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getItemImage()).isEqualTo("http://example.com/image1.jpg");
        assertThat(response.getCurrentWinnerName()).isEqualTo("John Doe");
        assertThat(response.getRecentBids()).hasSize(1);
    }

    @Test
    @DisplayName("getAuctionSessionDetail - fail: not found")
    void getAuctionSessionDetail_notFound() {
        // Arrange
        when(auctionSessionRepository.findByIdWithDetails(1L)).thenReturn(Optional.empty());

        // Act & Assert
        AppException ex = assertThrows(AppException.class, () -> auctionSessionService.getAuctionSessionDetail(1L));
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.SESSION_NOT_FOUND);
    }

    // ─── 2. placeBid Tests ────────────────────────────────────────────────

    @Test
    @DisplayName("placeBid - success: valid bid is saved and broadcast")
    void placeBid_success() {
        // Arrange
        PlaceBidRequest request = PlaceBidRequest.builder()
                .userId(1L)
                .bidAmount(BigDecimal.valueOf(110))
                .build();

        when(auctionSessionRepository.findByIdWithPessimisticLock(1L)).thenReturn(Optional.of(testSession));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(auctionParticipantRepository.findByUserIdAndSessionId(1L, 1L))
                .thenReturn(Optional.of(testParticipant));
        when(bidRepository.save(any(Bid.class))).thenAnswer(inv -> inv.getArgument(0));
        when(auctionSessionRepository.save(any(AuctionSession.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        auctionSessionService.placeBid(1L, request);

        // Assert
        verify(bidRepository).save(any(Bid.class));
        verify(auctionSessionRepository).save(any(AuctionSession.class));

        assertThat(testSession.getCurrentHighestBid()).isEqualByComparingTo(BigDecimal.valueOf(110));
        assertThat(testSession.getCurrentWinnerParticipant()).isEqualTo(testParticipant);
        assertThat(testSession.getBidCount()).isEqualTo(1);

        verify(eventPublisher).publishEvent(any(BidPlacedEvent.class));
    }

    @Test
    @DisplayName("placeBid - fail: session not found")
    void placeBid_fail_sessionNotFound() {
        // Arrange
        PlaceBidRequest request = PlaceBidRequest.builder().userId(1L).bidAmount(BigDecimal.valueOf(110)).build();
        when(auctionSessionRepository.findByIdWithPessimisticLock(1L)).thenReturn(Optional.empty());

        // Act & Assert
        AppException ex = assertThrows(AppException.class, () -> auctionSessionService.placeBid(1L, request));
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.SESSION_NOT_FOUND);
    }

    @Test
    @DisplayName("placeBid - fail: session is not active")
    void placeBid_fail_sessionNotActive() {
        // Arrange
        testSession.setStatus(SessionStatus.ENDED);
        PlaceBidRequest request = PlaceBidRequest.builder().userId(1L).bidAmount(BigDecimal.valueOf(110)).build();
        when(auctionSessionRepository.findByIdWithPessimisticLock(1L)).thenReturn(Optional.of(testSession));

        // Act & Assert
        AppException ex = assertThrows(AppException.class, () -> auctionSessionService.placeBid(1L, request));
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.SESSION_NOT_ACTIVE);
        verify(eventPublisher).publishEvent(any(BidPlacedEvent.class));
    }

    @Test
    @DisplayName("placeBid - fail: auction ended (time expired)")
    void placeBid_fail_auctionEnded() {
        // Arrange
        testSession.setEndTime(LocalDateTime.now().minusMinutes(1));
        PlaceBidRequest request = PlaceBidRequest.builder().userId(1L).bidAmount(BigDecimal.valueOf(110)).build();
        when(auctionSessionRepository.findByIdWithPessimisticLock(1L)).thenReturn(Optional.of(testSession));

        // Act & Assert
        AppException ex = assertThrows(AppException.class, () -> auctionSessionService.placeBid(1L, request));
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.AUCTION_ENDED);
        assertThat(testSession.getStatus()).isEqualTo(SessionStatus.ENDED);
        verify(auctionSessionRepository).save(testSession);
    }

    @Test
    @DisplayName("placeBid - fail: user not found")
    void placeBid_fail_userNotFound() {
        // Arrange
        PlaceBidRequest request = PlaceBidRequest.builder().userId(1L).bidAmount(BigDecimal.valueOf(110)).build();
        when(auctionSessionRepository.findByIdWithPessimisticLock(1L)).thenReturn(Optional.of(testSession));
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        AppException ex = assertThrows(AppException.class, () -> auctionSessionService.placeBid(1L, request));
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("placeBid - fail: bid amount is too low")
    void placeBid_fail_bidTooLow() {
        // Arrange
        PlaceBidRequest request = PlaceBidRequest.builder().userId(1L).bidAmount(BigDecimal.valueOf(105)).build();
        when(auctionSessionRepository.findByIdWithPessimisticLock(1L)).thenReturn(Optional.of(testSession));

        // Act & Assert
        AppException ex = assertThrows(AppException.class, () -> auctionSessionService.placeBid(1L, request));
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.INVALID_BID_AMOUNT);
    }

    @Test
    @DisplayName("placeBid - anti-snipe: extends auction time when bid placed within window")
    void placeBid_extendAuctionTime() {
        // Arrange
        LocalDateTime nearEndTime = LocalDateTime.now().plusSeconds(5); // within 10s anti-snipe window
        testSession.setEndTime(nearEndTime);
        testSession.setAntiSnipeWindowSeconds(10);
        testSession.setAntiSnipeExtensionSeconds(30);

        PlaceBidRequest request = PlaceBidRequest.builder().userId(1L).bidAmount(BigDecimal.valueOf(110)).build();

        when(auctionSessionRepository.findByIdWithPessimisticLock(1L)).thenReturn(Optional.of(testSession));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(auctionParticipantRepository.findByUserIdAndSessionId(1L, 1L)).thenReturn(Optional.of(testParticipant));
        when(bidRepository.save(any(Bid.class))).thenAnswer(inv -> inv.getArgument(0));
        when(auctionSessionRepository.save(any(AuctionSession.class))).thenAnswer(inv -> inv.getArgument(0));
        when(auctionExtensionLogRepository.save(any(AuctionExtensionLog.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        auctionSessionService.placeBid(1L, request);

        // Assert
        verify(bidRepository).save(any(Bid.class));
        verify(auctionSessionRepository).save(any(AuctionSession.class));
        verify(auctionExtensionLogRepository).save(any(AuctionExtensionLog.class));
        assertThat(testSession.getEndTime()).isAfter(nearEndTime);
    }

    // ─── 3. createSession Tests ───────────────────────────────────────────

    @Test
    @DisplayName("createSession - success")
    void createSession_success() {
        // Arrange
        AuctionSessionRequest request = AuctionSessionRequest.builder()
                .itemId(1L)
                .startTime(LocalDateTime.now().plusHours(1))
                .endTime(LocalDateTime.now().plusHours(2))
                .reservePrice(BigDecimal.valueOf(500))
                .minimumIncrement(BigDecimal.valueOf(50))
                .antiSnipeWindowSeconds(15)
                .antiSnipeExtensionSeconds(45)
                .build();

        when(lockService.executeWithLock(eq("item:1"), any())).thenAnswer(inv -> ((Supplier<?>) inv.getArgument(1)).get());
        when(transactionTemplate.execute(any())).thenAnswer(inv -> ((TransactionCallback<?>) inv.getArgument(0)).doInTransaction(null));
        when(auctionItemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(auctionSessionRepository.existsByItemIdAndStatusIn(eq(1L), anyList())).thenReturn(false);
        when(authenticationService.getCurrentUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(auctionSessionRepository.save(any(AuctionSession.class))).thenAnswer(inv -> {
            AuctionSession s = inv.getArgument(0);
            s.setId(10L);
            return s;
        });

        // Act
        AuctionSessionResponse response = auctionSessionService.createSession(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getItemId()).isEqualTo(1L);
        assertThat(response.getStatus()).isEqualTo(SessionStatus.SCHEDULED);
        verify(eventPublisher).publishEvent(any(AuditLogEvent.class));
        verify(eventPublisher).publishEvent(any(AuctionSessionCreatedEvent.class));
    }

    @Test
    @DisplayName("createSession - fail: item not found")
    void createSession_fail_itemNotFound() {
        // Arrange
        AuctionSessionRequest request = AuctionSessionRequest.builder().itemId(1L).build();
        when(lockService.executeWithLock(eq("item:1"), any())).thenAnswer(inv -> ((Supplier<?>) inv.getArgument(1)).get());
        when(transactionTemplate.execute(any())).thenAnswer(inv -> ((TransactionCallback<?>) inv.getArgument(0)).doInTransaction(null));
        when(auctionItemRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        AppException ex = assertThrows(AppException.class, () -> auctionSessionService.createSession(request));
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.ITEM_NOT_FOUND);
    }

    @Test
    @DisplayName("createSession - fail: item already sold")
    void createSession_fail_itemSold() {
        // Arrange
        testItem.setStatus(ItemStatus.SOLD);
        AuctionSessionRequest request = AuctionSessionRequest.builder().itemId(1L).build();
        when(lockService.executeWithLock(eq("item:1"), any())).thenAnswer(inv -> ((Supplier<?>) inv.getArgument(1)).get());
        when(transactionTemplate.execute(any())).thenAnswer(inv -> ((TransactionCallback<?>) inv.getArgument(0)).doInTransaction(null));
        when(auctionItemRepository.findById(1L)).thenReturn(Optional.of(testItem));

        // Act & Assert
        AppException ex = assertThrows(AppException.class, () -> auctionSessionService.createSession(request));
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.ITEM_ALREADY_SOLD);
    }

    @Test
    @DisplayName("createSession - fail: item not approved")
    void createSession_fail_itemNotApproved() {
        // Arrange
        testItem.setStatus(ItemStatus.PENDING);
        AuctionSessionRequest request = AuctionSessionRequest.builder().itemId(1L).build();
        when(lockService.executeWithLock(eq("item:1"), any())).thenAnswer(inv -> ((Supplier<?>) inv.getArgument(1)).get());
        when(transactionTemplate.execute(any())).thenAnswer(inv -> ((TransactionCallback<?>) inv.getArgument(0)).doInTransaction(null));
        when(auctionItemRepository.findById(1L)).thenReturn(Optional.of(testItem));

        // Act & Assert
        AppException ex = assertThrows(AppException.class, () -> auctionSessionService.createSession(request));
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.ITEM_NOT_APPROVED);
    }

    @Test
    @DisplayName("createSession - fail: session conflict")
    void createSession_fail_sessionConflict() {
        // Arrange
        AuctionSessionRequest request = AuctionSessionRequest.builder().itemId(1L).build();
        when(lockService.executeWithLock(eq("item:1"), any())).thenAnswer(inv -> ((Supplier<?>) inv.getArgument(1)).get());
        when(transactionTemplate.execute(any())).thenAnswer(inv -> ((TransactionCallback<?>) inv.getArgument(0)).doInTransaction(null));
        when(auctionItemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(auctionSessionRepository.existsByItemIdAndStatusIn(eq(1L), anyList())).thenReturn(true);

        // Act & Assert
        AppException ex = assertThrows(AppException.class, () -> auctionSessionService.createSession(request));
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.SESSION_CONFLICT);
    }

    // ─── 4. getSessions Tests ─────────────────────────────────────────────

    @Test
    @DisplayName("getSessions - success")
    void getSessions_success() {
        // Arrange
        List<AuctionSessionListResponse> list = new ArrayList<>();
        list.add(AuctionSessionListResponse.builder().id(1L).build());
        list.add(AuctionSessionListResponse.builder().id(2L).build());

        when(auctionSessionRepository.findOptimizedSessions(eq(null), eq(true), eq(SessionStatus.ACTIVE), eq(true), eq("phone"), any(Pageable.class)))
                .thenReturn(list);

        // Act
        CursorPageResponse<AuctionSessionListResponse> response = auctionSessionService.getSessions(null, 1, "phone", SessionStatus.ACTIVE);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1); // 1 item removed because size > pageSize
        assertThat(response.isHasNext()).isTrue();
        assertThat(response.getNextCursor()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getSessions - fail: search keyword too short")
    void getSessions_fail_keywordTooShort() {
        // Act & Assert
        AppException ex = assertThrows(AppException.class, () -> auctionSessionService.getSessions(null, 10, "a", null));
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.SEARCH_KEYWORD_TOO_SHORT);
    }

    // ─── 5. getSessionById Tests ──────────────────────────────────────────

    @Test
    @DisplayName("getSessionById - success")
    void getSessionById_success() {
        // Arrange
        when(auctionSessionRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(testSession));

        // Act
        AuctionSessionResponse response = auctionSessionService.getSessionById(1L);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getItemName()).isEqualTo("Test Item");
    }

    @Test
    @DisplayName("getSessionById - fail: not found")
    void getSessionById_notFound() {
        when(auctionSessionRepository.findByIdWithDetails(1L)).thenReturn(Optional.empty());
        AppException ex = assertThrows(AppException.class, () -> auctionSessionService.getSessionById(1L));
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.SESSION_NOT_FOUND);
    }

    // ─── 6. updateSession Tests ───────────────────────────────────────────

    @Test
    @DisplayName("updateSession - success: active session extends endTime")
    void updateSession_success_active() {
        // Arrange
        LocalDateTime newEndTime = LocalDateTime.now().plusHours(5);
        AuctionSessionUpdateRequest request = AuctionSessionUpdateRequest.builder()
                .endTime(newEndTime)
                .build();

        when(auctionSessionRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(testSession));
        when(auctionSessionRepository.save(any(AuctionSession.class))).thenAnswer(inv -> inv.getArgument(0));
        when(authenticationService.getCurrentUserId()).thenReturn(1L);

        // Act
        AuctionSessionResponse response = auctionSessionService.updateSession(1L, request);

        // Assert
        assertThat(response.getEndTime()).isEqualTo(newEndTime);
        verify(eventPublisher).publishEvent(any(AuditLogEvent.class));
        verify(eventPublisher).publishEvent(any(SessionExtendedEvent.class));
    }

    @Test
    @DisplayName("updateSession - fail: rollback end_time for ACTIVE session")
    void updateSession_fail_rollbackEndTime() {
        // Arrange
        AuctionSessionUpdateRequest request = AuctionSessionUpdateRequest.builder()
                .endTime(LocalDateTime.now().minusMinutes(1))
                .build();

        when(auctionSessionRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(testSession));

        // Act & Assert
        AppException ex = assertThrows(AppException.class, () -> auctionSessionService.updateSession(1L, request));
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.SESSION_CANNOT_ROLLBACK_END_TIME);
    }

    @Test
    @DisplayName("updateSession - success: scheduled session update fields and cancel")
    void updateSession_success_scheduled() {
        // Arrange
        testSession.setStatus(SessionStatus.SCHEDULED);
        AuctionSessionUpdateRequest request = AuctionSessionUpdateRequest.builder()
                .reservePrice(BigDecimal.valueOf(2000))
                .minimumIncrement(BigDecimal.valueOf(100))
                .antiSnipeWindowSeconds(20)
                .antiSnipeExtensionSeconds(60)
                .status(SessionStatus.CANCELLED)
                .cancellationReason("Item damaged")
                .build();

        when(auctionSessionRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(testSession));
        when(auctionSessionRepository.save(any(AuctionSession.class))).thenAnswer(inv -> inv.getArgument(0));
        when(authenticationService.getCurrentUserId()).thenReturn(1L);

        // Act
        AuctionSessionResponse response = auctionSessionService.updateSession(1L, request);

        // Assert
        assertThat(response.getReservePrice()).isEqualByComparingTo(BigDecimal.valueOf(2000));
        assertThat(response.getMinimumIncrement()).isEqualByComparingTo(BigDecimal.valueOf(100));
        assertThat(response.getAntiSnipeWindowSeconds()).isEqualTo(20);
        assertThat(response.getAntiSnipeExtensionSeconds()).isEqualTo(60);
        assertThat(response.getStatus()).isEqualTo(SessionStatus.CANCELLED);
        assertThat(response.getCancellationReason()).isEqualTo("Item damaged");
    }

    @Test
    @DisplayName("updateSession - fail: invalid endTime for scheduled session")
    void updateSession_fail_invalidEndTimeScheduled() {
        // Arrange
        testSession.setStatus(SessionStatus.SCHEDULED);
        AuctionSessionUpdateRequest request = AuctionSessionUpdateRequest.builder()
                .endTime(testSession.getStartTime().minusHours(1))
                .build();

        when(auctionSessionRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(testSession));

        // Act & Assert
        AppException ex = assertThrows(AppException.class, () -> auctionSessionService.updateSession(1L, request));
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.SESSION_INVALID_END_TIME);
    }

    // ─── 7. deleteSession Tests ───────────────────────────────────────────

    @Test
    @DisplayName("deleteSession - success")
    void deleteSession_success() {
        // Arrange
        testSession.setStatus(SessionStatus.SCHEDULED);
        when(auctionSessionRepository.findById(1L)).thenReturn(Optional.of(testSession));
        when(authenticationService.getCurrentUserId()).thenReturn(1L);

        // Act
        auctionSessionService.deleteSession(1L);

        // Assert
        verify(auctionSessionRepository).delete(testSession);
        verify(eventPublisher).publishEvent(any(AuditLogEvent.class));
    }

    @Test
    @DisplayName("deleteSession - fail: ACTIVE session")
    void deleteSession_fail_active() {
        // Arrange
        testSession.setStatus(SessionStatus.ACTIVE);
        when(auctionSessionRepository.findById(1L)).thenReturn(Optional.of(testSession));

        // Act & Assert
        AppException ex = assertThrows(AppException.class, () -> auctionSessionService.deleteSession(1L));
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.SESSION_ALREADY_ACTIVE);
    }

    @Test
    @DisplayName("deleteSession - fail: not found")
    void deleteSession_notFound() {
        when(auctionSessionRepository.findById(1L)).thenReturn(Optional.empty());
        AppException ex = assertThrows(AppException.class, () -> auctionSessionService.deleteSession(1L));
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.SESSION_NOT_FOUND);
    }
}
