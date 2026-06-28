package hoang.com.auction_system_be.service.bid;

import hoang.com.auction_system_be.dto.response.BidLogResponse;
import hoang.com.auction_system_be.dto.response.PageResponse;
import hoang.com.auction_system_be.entity.Bid;
import hoang.com.auction_system_be.mapper.BidMapper;
import hoang.com.auction_system_be.repository.BidRepository;
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

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BidServiceImpl implements BidService {

    BidRepository bidRepository;
    BidMapper bidMapper;
    SecurityContextService securityContextService;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<BidLogResponse> getBidLogs(int page, int size, Long sessionId, Long userId) {
        // Only Staff/Admin can view full logs
        securityContextService.checkAdminOrManagerUser();

        Pageable pageable = PageRequest.of(page, size, Sort.by("bidTimestamp").descending());

        Specification<Bid> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (sessionId != null) {
                predicates.add(cb.equal(root.get("participant").get("session").get("id"), sessionId));
            }
            if (userId != null) {
                predicates.add(cb.equal(root.get("participant").get("user").get("id"), userId));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Bid> bidPage = bidRepository.findAll(spec, pageable);

        return PageResponse.<BidLogResponse>builder()
                .pageNo(bidPage.getNumber())
                .totalPages(bidPage.getTotalPages())
                .pageSize(bidPage.getSize())
                .totalElements(bidPage.getTotalElements())
                .last(bidPage.isLast())
                .content(bidPage.getContent().stream().map(bidMapper::toBidLogResponse).toList())
                .build();
    }
}
