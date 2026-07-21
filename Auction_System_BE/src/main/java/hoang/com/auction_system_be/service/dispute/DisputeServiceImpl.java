package hoang.com.auction_system_be.service.dispute;

import hoang.com.auction_system_be.dto.request.DisputeRequest;
import hoang.com.auction_system_be.dto.request.DisputeResolutionRequest;
import hoang.com.auction_system_be.dto.response.DisputeResponse;
import hoang.com.auction_system_be.dto.response.PageResponse;
import hoang.com.auction_system_be.entity.AuctionSession;
import hoang.com.auction_system_be.entity.Dispute;
import hoang.com.auction_system_be.entity.User;
import hoang.com.auction_system_be.enums.DisputeStatus;
import hoang.com.auction_system_be.exception.AppException;
import hoang.com.auction_system_be.exception.ErrorCode;
import hoang.com.auction_system_be.mapper.DisputeMapper;
import hoang.com.auction_system_be.repository.AuctionSessionRepository;
import hoang.com.auction_system_be.repository.DisputeRepository;
import hoang.com.auction_system_be.service.auth.SecurityContextService;
import jakarta.persistence.criteria.Predicate;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DisputeServiceImpl implements DisputeService {

    DisputeRepository disputeRepository;
    AuctionSessionRepository auctionSessionRepository;
    SecurityContextService securityContextService;
    DisputeMapper disputeMapper;

    @Override
    @Transactional
    public DisputeResponse createDispute(DisputeRequest request) {
        User currentUser = securityContextService.getCurrentUserEntity();

        AuctionSession session = auctionSessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> new AppException(ErrorCode.SESSION_NOT_FOUND));

        Dispute dispute = Dispute.builder()
                .session(session)
                .raisedBy(currentUser)
                .type(request.getType())
                .description(request.getDescription())
                .status(DisputeStatus.OPEN)
                .build();

        Dispute savedDispute = disputeRepository.save(dispute);
        return disputeMapper.toResponse(savedDispute);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DisputeResponse> getDisputes(int page, int size, Long sessionId, Long raisedById) {
        // Only Staff/Admin can view all disputes broadly
        securityContextService.checkAdminOrManagerUser();

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Specification<Dispute> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (sessionId != null) {
                predicates.add(cb.equal(root.get("session").get("id"), sessionId));
            }
            if (raisedById != null) {
                predicates.add(cb.equal(root.get("raisedBy").get("id"), raisedById));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Dispute> disputePage = disputeRepository.findAll(spec, pageable);

        return PageResponse.<DisputeResponse>builder()
                .pageNo(disputePage.getNumber())
                .totalPages(disputePage.getTotalPages())
                .pageSize(disputePage.getSize())
                .totalElements(disputePage.getTotalElements())
                .last(disputePage.isLast())
                .content(disputePage.getContent().stream().map(disputeMapper::toResponse).toList())
                .build();
    }

    @Override
    @Transactional
    public DisputeResponse updateDisputeStatus(Long id, DisputeResolutionRequest request) {
        User manager = securityContextService.checkAdminOrManagerUser();

        Dispute dispute = disputeRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.DISPUTE_NOT_FOUND));

        dispute.setStatus(request.getStatus());
        dispute.setResolution(request.getResolution());
        dispute.setAssignedManager(manager);
        
        if (request.getStatus() == DisputeStatus.RESOLVED || request.getStatus() == DisputeStatus.REJECTED) {
            dispute.setResolvedAt(LocalDateTime.now());
        }

        Dispute savedDispute = disputeRepository.save(dispute);
        return disputeMapper.toResponse(savedDispute);
    }
}
