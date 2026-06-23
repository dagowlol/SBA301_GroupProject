package hoang.com.auction_system_be.service.impl;

import hoang.com.auction_system_be.dto.request.DisputeRequest;
import hoang.com.auction_system_be.dto.request.DisputeResolutionRequest;
import hoang.com.auction_system_be.dto.response.DisputeResponse;
import hoang.com.auction_system_be.entity.AuctionSession;
import hoang.com.auction_system_be.entity.Dispute;
import hoang.com.auction_system_be.entity.User;
import hoang.com.auction_system_be.enums.DisputeStatus;
import hoang.com.auction_system_be.enums.RoleName;
import hoang.com.auction_system_be.exception.AppException;
import hoang.com.auction_system_be.exception.ErrorCode;
import hoang.com.auction_system_be.mapper.DisputeMapper;
import hoang.com.auction_system_be.repository.AuctionSessionRepository;
import hoang.com.auction_system_be.repository.DisputeRepository;
import hoang.com.auction_system_be.service.auth.SecurityContextService;
import hoang.com.auction_system_be.service.dispute.DisputeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DisputeServiceImplTest {

    @Mock
    DisputeRepository disputeRepository;

    @Mock
    AuctionSessionRepository auctionSessionRepository;

    @Mock
    SecurityContextService securityContextService;

    @Spy
    DisputeMapper disputeMapper = new DisputeMapper();

    @InjectMocks
    DisputeServiceImpl disputeService;

    User testUser;
    User testAdmin;
    AuctionSession testSession;
    Dispute testDispute;

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(1L).firstName("Normal").lastName("User").role(RoleName.USER).build();
        testAdmin = User.builder().id(2L).firstName("Admin").lastName("User").role(RoleName.ADMIN).build();
        testSession = AuctionSession.builder().id(1L).build();
        
        testDispute = Dispute.builder()
                .id(1L)
                .session(testSession)
                .raisedBy(testUser)
                .type("PAYMENT_ISSUE")
                .description("Did not pay")
                .status(DisputeStatus.OPEN)
                .build();
    }

    @Test
    void createDispute_success() {
        DisputeRequest request = new DisputeRequest(1L, "PAYMENT_ISSUE", "Did not pay");

        when(securityContextService.getCurrentUserEntity()).thenReturn(testUser);
        when(auctionSessionRepository.findById(1L)).thenReturn(Optional.of(testSession));
        when(disputeRepository.save(any(Dispute.class))).thenAnswer(inv -> {
            Dispute d = inv.getArgument(0);
            d.setId(1L);
            return d;
        });

        DisputeResponse response = disputeService.createDispute(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getType()).isEqualTo("PAYMENT_ISSUE");
        assertThat(response.getStatus()).isEqualTo(DisputeStatus.OPEN);
    }

    @Test
    void updateDisputeStatus_success() {
        DisputeResolutionRequest request = new DisputeResolutionRequest(DisputeStatus.RESOLVED, "Refunded");

        when(securityContextService.checkAdminOrManagerUser()).thenReturn(testAdmin);
        when(disputeRepository.findById(1L)).thenReturn(Optional.of(testDispute));
        when(disputeRepository.save(any(Dispute.class))).thenAnswer(inv -> inv.getArgument(0));

        DisputeResponse response = disputeService.updateDisputeStatus(1L, request);

        assertThat(response.getStatus()).isEqualTo(DisputeStatus.RESOLVED);
        assertThat(response.getResolution()).isEqualTo("Refunded");
        assertThat(response.getAssignedManagerId()).isEqualTo(testAdmin.getId());
    }

    @Test
    void updateDisputeStatus_unauthorized() {
        DisputeResolutionRequest request = new DisputeResolutionRequest(DisputeStatus.RESOLVED, "Refunded");

        when(securityContextService.checkAdminOrManagerUser()).thenThrow(new AppException(ErrorCode.UNAUTHORIZED));

        AppException exception = assertThrows(AppException.class, () -> {
            disputeService.updateDisputeStatus(1L, request);
        });

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.UNAUTHORIZED);
        verify(disputeRepository, never()).save(any());
    }
}
