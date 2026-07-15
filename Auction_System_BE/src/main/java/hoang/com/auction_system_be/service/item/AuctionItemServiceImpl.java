package hoang.com.auction_system_be.service.item;

import hoang.com.auction_system_be.dto.request.ItemRejectRequest;
import hoang.com.auction_system_be.dto.request.ItemRequest;
import hoang.com.auction_system_be.dto.request.UpdateItemRequest;
import hoang.com.auction_system_be.dto.response.ItemResponse;
import hoang.com.auction_system_be.dto.response.PageResponse;
import hoang.com.auction_system_be.entity.AuctionItem;
import hoang.com.auction_system_be.entity.Category;
import hoang.com.auction_system_be.entity.User;
import hoang.com.auction_system_be.entity.ItemImage;
import hoang.com.auction_system_be.entity.AuctionSession;
import hoang.com.auction_system_be.entity.Payment;
import hoang.com.auction_system_be.enums.ItemStatus;
import hoang.com.auction_system_be.enums.SessionStatus;

import hoang.com.auction_system_be.exception.AppException;
import hoang.com.auction_system_be.exception.ErrorCode;
import hoang.com.auction_system_be.mapper.ItemMapper;
import hoang.com.auction_system_be.repository.AuctionItemRepository;
import hoang.com.auction_system_be.repository.CategoryRepository;
import hoang.com.auction_system_be.repository.PaymentRepository;
import hoang.com.auction_system_be.service.auth.SecurityContextService;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AuctionItemServiceImpl implements AuctionItemService {

    AuctionItemRepository itemRepository;
    CategoryRepository categoryRepository;
    ItemMapper itemMapper;
    SecurityContextService securityContextService;
    PaymentRepository paymentRepository;

    @Override
    @Transactional
    public ItemResponse createItem(ItemRequest request) {
        User seller = securityContextService.getCurrentUserEntity();
        Long sellerId = seller.getId();

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        AuctionItem item = AuctionItem.builder()
                .name(request.getName())
                .description(request.getDescription())
                .startingPrice(request.getStartingPrice())
                .reservePrice(request.getReservePrice())
                .category(category)
                .seller(seller)
                .status(ItemStatus.PENDING)
                .build();

        AuctionItem savedItem = itemRepository.save(item);
        log.info("Created new item with id: {} by seller id: {}", savedItem.getId(), sellerId);
        return itemMapper.toResponse(savedItem);
    }

    @Override
    @Transactional
    public ItemResponse approveItem(Long itemId) {
        User reviewer = securityContextService.checkAdminOrManagerUser();
        Long reviewerId = reviewer.getId();

        AuctionItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new AppException(ErrorCode.ITEM_NOT_FOUND));

        if (item.getStatus() != ItemStatus.PENDING) {
            throw new AppException(ErrorCode.INVALID_ITEM_STATUS);
        }

        item.setStatus(ItemStatus.APPROVED);
        item.setReviewedBy(reviewer);
        item.setReviewedAt(LocalDateTime.now());

        AuctionItem updatedItem = itemRepository.save(item);
        log.info("Item {} approved by reviewer {}", itemId, reviewerId);
        return itemMapper.toResponse(updatedItem);
    }

    @Override
    @Transactional
    public ItemResponse rejectItem(Long itemId, ItemRejectRequest request) {
        User reviewer = securityContextService.checkAdminOrManagerUser();
        Long reviewerId = reviewer.getId();

        AuctionItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new AppException(ErrorCode.ITEM_NOT_FOUND));

        if (item.getStatus() != ItemStatus.PENDING) {
            throw new AppException(ErrorCode.INVALID_ITEM_STATUS);
        }

        item.setStatus(ItemStatus.REJECTED);
        item.setReviewedBy(reviewer);
        item.setReviewedAt(LocalDateTime.now());
        item.setRejectionReason(request.getRejectionReason());

        AuctionItem updatedItem = itemRepository.save(item);
        log.info("Item {} rejected by reviewer {}", itemId, reviewerId);
        return itemMapper.toResponse(updatedItem);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ItemResponse> getItems(int page, int size, String name, Long categoryId, ItemStatus status) {
        User requester = securityContextService.getCurrentUserEntity();
        Long requesterId = requester.getId();

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Specification<AuctionItem> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (name != null && !name.trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
            }

            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            // if (requester.getRole() == RoleName.USER) {
            // predicates.add(cb.equal(root.get("seller").get("id"), requesterId));
            // }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<AuctionItem> itemPage = itemRepository.findAll(spec, pageable);
        List<ItemResponse> content = itemPage.getContent().stream()
                .map(itemMapper::toResponse)
                .collect(Collectors.toList());

        return PageResponse.<ItemResponse>builder()
                .content(content)
                .pageNo(itemPage.getNumber())
                .pageSize(itemPage.getSize())
                .totalElements(itemPage.getTotalElements())
                .totalPages(itemPage.getTotalPages())
                .last(itemPage.isLast())
                .build();
    }

    @Override
    @Transactional
    public ItemResponse updateItem(Long itemId, UpdateItemRequest request) {
        AuctionItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new AppException(ErrorCode.ITEM_NOT_FOUND));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        item.setName(request.getName());
        item.setDescription(request.getDescription());
        item.setStartingPrice(request.getStartingPrice());
        item.setReservePrice(request.getReservePrice());
        item.setCategory(category);
        if (request.getStatus() != null) {
            item.setStatus(request.getStatus());
        }

        if (request.getImageUrl() != null && !request.getImageUrl().trim().isEmpty()) {
            item.getImages().clear();
            item.getImages().add(ItemImage.builder()
                    .item(item)
                    .imageUrl(request.getImageUrl())
                    .isPrimary(true)
                    .build());
        }

        if (request.getSubmittedBy() != null && !request.getSubmittedBy().trim().isEmpty()
                && item.getSeller() != null) {
            String[] parts = request.getSubmittedBy().trim().split("\\s+", 2);
            if (parts.length > 0) {
                item.getSeller().setFirstName(parts[0]);
            }
            if (parts.length > 1) {
                item.getSeller().setLastName(parts[1]);
            } else {
                item.getSeller().setLastName("");
            }
        }

        if (item.getSessions() != null && !item.getSessions().isEmpty()) {
            AuctionSession session = item.getSessions().get(0);
            if (request.getStartTime() != null) {
                session.setStartTime(request.getStartTime());
            }
            if (request.getEndTime() != null) {
                session.setEndTime(request.getEndTime());
            }
            if (request.getReservePrice() != null) {
                session.setReservePrice(request.getReservePrice());
            }
        }

        AuctionItem updatedItem = itemRepository.save(item);
        log.info("Updated item with id: {}", updatedItem.getId());
        return itemMapper.toResponse(updatedItem);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ItemResponse> getMyUploadedItems(int page, int size, String name, Long categoryId, ItemStatus status) {
        User requester = securityContextService.getCurrentUserEntity();
        Long requesterId = requester.getId();

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Specification<AuctionItem> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("seller").get("id"), requesterId));

            if (name != null && !name.trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
            }

            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<AuctionItem> itemPage = itemRepository.findAll(spec, pageable);
        List<ItemResponse> content = itemPage.getContent().stream()
                .map(itemMapper::toResponse)
                .collect(Collectors.toList());

        return PageResponse.<ItemResponse>builder()
                .content(content)
                .pageNo(itemPage.getNumber())
                .pageSize(itemPage.getSize())
                .totalElements(itemPage.getTotalElements())
                .totalPages(itemPage.getTotalPages())
                .last(itemPage.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ItemResponse> getMyWonItems(int page, int size, String name, Long categoryId) {
        User requester = securityContextService.getCurrentUserEntity();
        Long requesterId = requester.getId();

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Specification<AuctionItem> spec = (root, query, cb) -> {
            // Use EXISTS subquery to avoid DISTINCT on TEXT columns (SQL Server limitation)
            List<Predicate> predicates = new ArrayList<>();

            Subquery<Long> subquery = query.subquery(Long.class);
            Root<AuctionSession> sessionRoot = subquery.from(AuctionSession.class);
            subquery.select(sessionRoot.get("item").get("id"));
            subquery.where(
                cb.equal(sessionRoot.get("status"), SessionStatus.ENDED),
                cb.equal(sessionRoot.get("currentWinnerParticipant").get("user").get("id"), requesterId),
                cb.equal(sessionRoot.get("item").get("id"), root.get("id"))
            );
            predicates.add(cb.exists(subquery));

            if (name != null && !name.trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
            }

            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<AuctionItem> itemPage = itemRepository.findAll(spec, pageable);
        List<ItemResponse> content = itemPage.getContent().stream()
                .map(item -> {
                    ItemResponse response = itemMapper.toResponse(item);
                    // Find the ENDED session for this item where current user is the winner,
                    // then attach the payment info (id + status)
                    item.getSessions().stream()
                            .filter(s -> s.getStatus() == SessionStatus.ENDED
                                    && s.getCurrentWinnerParticipant() != null
                                    && s.getCurrentWinnerParticipant().getUser().getId().equals(requesterId))
                            .findFirst()
                            .ifPresent(session -> {
                                paymentRepository
                                        .findByParticipantUserIdAndParticipantSessionId(requesterId, session.getId())
                                        .ifPresent(payment -> {
                                            response.setPaymentId(payment.getId());
                                            response.setPaymentStatus(payment.getStatus());
                                        });
                            });
                    return response;
                })
                .collect(Collectors.toList());

        return PageResponse.<ItemResponse>builder()
                .content(content)
                .pageNo(itemPage.getNumber())
                .pageSize(itemPage.getSize())
                .totalElements(itemPage.getTotalElements())
                .totalPages(itemPage.getTotalPages())
                .last(itemPage.isLast())
                .build();
    }
}

