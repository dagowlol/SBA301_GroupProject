package hoang.com.auction_system_be.service.impl;

import hoang.com.auction_system_be.dto.request.PlaceBidRequest;
import hoang.com.auction_system_be.dto.response.BidBroadcastResponse;
import hoang.com.auction_system_be.dto.response.ErrorSocketResponse;
import hoang.com.auction_system_be.entity.*;
import hoang.com.auction_system_be.enums.SessionStatus;
import hoang.com.auction_system_be.repository.*;
import hoang.com.auction_system_be.service.session.AuctionSessionServiceImpl;
import hoang.com.auction_system_be.mapper.AuctionSessionMapper;
import hoang.com.auction_system_be.mapper.BidMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
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
        AuctionParticipantRepository auctionParticipantRepository;

        @Mock
        AuctionExtensionLogRepository auctionExtensionLogRepository;

        @Mock
        SimpMessagingTemplate messagingTemplate;

        @Spy
        AuctionSessionMapper auctionSessionMapper = new AuctionSessionMapper();

        @Spy
        BidMapper bidMapper = new BidMapper();

        @Mock
        org.springframework.context.ApplicationEventPublisher eventPublisher;

        @Mock
        hoang.com.auction_system_be.service.autobid.AutoBidService autoBidService;

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

                testItem = AuctionItem.builder()
                                .id(1L)
                                .name("Test Item")
                                .description("Test Description")
                                .startingPrice(BigDecimal.valueOf(100))
                                .images(new ArrayList<>())
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
                                .participants(new ArrayList<>())
                                .build();

                testParticipant = AuctionParticipant.builder()
                                .id(1L)
                                .user(testUser)
                                .session(testSession)
                                .build();
		}

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
                when(auctionSessionRepository.save(any(AuctionSession.class)))
                                .thenAnswer(inv -> inv.getArgument(0));

                // Act
                auctionSessionService.placeBid(1L, request);

                // Assert
                verify(bidRepository).save(any(Bid.class));
                verify(auctionSessionRepository).save(any(AuctionSession.class));

                assertThat(testSession.getCurrentHighestBid()).isEqualByComparingTo(BigDecimal.valueOf(110));
                assertThat(testSession.getCurrentWinnerParticipant()).isEqualTo(testParticipant);
                assertThat(testSession.getBidCount()).isEqualTo(1);

                // Verify event published
                ArgumentCaptor<hoang.com.auction_system_be.event.BidPlacedEvent> eventCaptor = ArgumentCaptor
                                .forClass(hoang.com.auction_system_be.event.BidPlacedEvent.class);
                verify(eventPublisher).publishEvent(eventCaptor.capture());

                hoang.com.auction_system_be.event.BidPlacedEvent event = eventCaptor.getValue();
                assertThat(event.getSessionId()).isEqualTo("1");
                assertThat(event.isError()).isFalse();
                BidBroadcastResponse broadcast = event.getBroadcastResponse();
                assertThat(broadcast.getSessionId()).isEqualTo(1L);
                assertThat(broadcast.getCurrentPrice()).isEqualByComparingTo(BigDecimal.valueOf(110));
                assertThat(broadcast.getWinnerName()).isEqualTo("John Doe");
        }

        @Test
        @DisplayName("placeBid - fail: session is not active")
        void placeBid_fail_sessionNotActive() {
                // Arrange
                testSession.setStatus(SessionStatus.ENDED);

                PlaceBidRequest request = PlaceBidRequest.builder()
                                .userId(1L)
                                .bidAmount(BigDecimal.valueOf(110))
                                .build();

                when(auctionSessionRepository.findByIdWithPessimisticLock(1L)).thenReturn(Optional.of(testSession));

                // Act & Assert
                org.junit.jupiter.api.Assertions.assertThrows(hoang.com.auction_system_be.exception.AppException.class, () -> {
                        auctionSessionService.placeBid(1L, request);
                });

                // Assert - no bid saved
                verify(bidRepository, never()).save(any(Bid.class));
                verify(auctionSessionRepository, never()).save(any(AuctionSession.class));

                ArgumentCaptor<hoang.com.auction_system_be.event.BidPlacedEvent> eventCaptor = ArgumentCaptor
                                .forClass(hoang.com.auction_system_be.event.BidPlacedEvent.class);
                verify(eventPublisher).publishEvent(eventCaptor.capture());

                hoang.com.auction_system_be.event.BidPlacedEvent event = eventCaptor.getValue();
                assertThat(event.isError()).isTrue();
                assertThat(event.getErrorMsg()).isEqualTo("Auction session is not active");
        }

        @Test
        @DisplayName("placeBid - fail: bid amount is too low")
        void placeBid_fail_bidTooLow() {
                // Arrange
                PlaceBidRequest request = PlaceBidRequest.builder()
                                .userId(1L)
                                .bidAmount(BigDecimal.valueOf(105)) // needs at least 110 (100 + 10 increment)
                                .build();

                when(auctionSessionRepository.findByIdWithPessimisticLock(1L)).thenReturn(Optional.of(testSession));

                // Act & Assert
                org.junit.jupiter.api.Assertions.assertThrows(hoang.com.auction_system_be.exception.AppException.class, () -> {
                        auctionSessionService.placeBid(1L, request);
                });

                // Assert - no bid saved
                verify(bidRepository, never()).save(any(Bid.class));
                verify(auctionSessionRepository, never()).save(any(AuctionSession.class));

                ArgumentCaptor<hoang.com.auction_system_be.event.BidPlacedEvent> eventCaptor = ArgumentCaptor
                                .forClass(hoang.com.auction_system_be.event.BidPlacedEvent.class);
                verify(eventPublisher).publishEvent(eventCaptor.capture());

                hoang.com.auction_system_be.event.BidPlacedEvent event = eventCaptor.getValue();
                assertThat(event.isError()).isTrue();
                assertThat(event.getErrorMsg()).isEqualTo("Bid amount is too low");
        }

        @Test
        @DisplayName("placeBid - anti-snipe: extends auction time when bid placed within 30 seconds of end")
        void placeBid_extendAuctionTime() {
                // Arrange - session ends in 20 seconds (within 30s anti-snipe window)
                LocalDateTime nearEndTime = LocalDateTime.now().plusSeconds(20);
                testSession.setEndTime(nearEndTime);
                testSession.setAntiSnipeWindowSeconds(30);

                PlaceBidRequest request = PlaceBidRequest.builder()
                                .userId(1L)
                                .bidAmount(BigDecimal.valueOf(110))
                                .build();

                when(auctionSessionRepository.findByIdWithPessimisticLock(1L)).thenReturn(Optional.of(testSession));
                when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
                when(auctionParticipantRepository.findByUserIdAndSessionId(1L, 1L))
                                .thenReturn(Optional.of(testParticipant));
                when(bidRepository.save(any(Bid.class))).thenAnswer(inv -> inv.getArgument(0));
                when(auctionSessionRepository.save(any(AuctionSession.class)))
                                .thenAnswer(inv -> inv.getArgument(0));
                when(auctionExtensionLogRepository.save(any(AuctionExtensionLog.class)))
                                .thenAnswer(inv -> inv.getArgument(0));

                // Act
                auctionSessionService.placeBid(1L, request);

                // Assert
                verify(bidRepository).save(any(Bid.class));
                verify(auctionSessionRepository).save(any(AuctionSession.class));
                verify(auctionExtensionLogRepository).save(any(AuctionExtensionLog.class));

                // End time should have been extended by 30 seconds
                assertThat(testSession.getEndTime()).isAfter(nearEndTime);

                // Verify extension log was saved with correct data
                ArgumentCaptor<AuctionExtensionLog> logCaptor = ArgumentCaptor.forClass(AuctionExtensionLog.class);
                verify(auctionExtensionLogRepository).save(logCaptor.capture());

                AuctionExtensionLog savedLog = logCaptor.getValue();
                assertThat(savedLog.getOldEndTime()).isEqualTo(nearEndTime);
                assertThat(savedLog.getNewEndTime()).isEqualTo(nearEndTime.plusSeconds(30));
                assertThat(savedLog.getSession()).isEqualTo(testSession);
                assertThat(savedLog.getTriggeredByParticipant()).isEqualTo(testParticipant);

                // Verify event published
                ArgumentCaptor<hoang.com.auction_system_be.event.BidPlacedEvent> eventCaptor = ArgumentCaptor
                                .forClass(hoang.com.auction_system_be.event.BidPlacedEvent.class);
                verify(eventPublisher).publishEvent(eventCaptor.capture());

                hoang.com.auction_system_be.event.BidPlacedEvent event = eventCaptor.getValue();
                assertThat(event.getSessionId()).isEqualTo("1");
                assertThat(event.isError()).isFalse();
        }
}
