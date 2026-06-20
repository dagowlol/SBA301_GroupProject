package hoang.com.auction_system_be.service;

import hoang.com.auction_system_be.dto.request.ItemRejectRequest;
import hoang.com.auction_system_be.dto.request.ItemRequest;
import hoang.com.auction_system_be.dto.response.ItemResponse;
import hoang.com.auction_system_be.dto.response.PageResponse;
import hoang.com.auction_system_be.entity.AuctionItem;
import hoang.com.auction_system_be.entity.Category;
import hoang.com.auction_system_be.entity.User;
import hoang.com.auction_system_be.enums.ItemStatus;
import hoang.com.auction_system_be.enums.RoleName;
import hoang.com.auction_system_be.exception.AppException;
import hoang.com.auction_system_be.exception.ErrorCode;
import hoang.com.auction_system_be.mapper.ItemMapper;
import hoang.com.auction_system_be.repository.AuctionItemRepository;
import hoang.com.auction_system_be.repository.CategoryRepository;
import hoang.com.auction_system_be.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
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
    UserRepository userRepository;
    ItemMapper itemMapper;
    AuthenticationService authenticationService;

    @Override
    @Transactional
    public ItemResponse createItem(ItemRequest request) {
        Long sellerId = authenticationService.getCurrentUserId();
        User seller = userRepository.findById(sellerId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        AuctionItem item = AuctionItem.builder()
                .name(request.getName())
                .description(request.getDescription())
                .startingPrice(request.getStartingPrice())
                .reservePrice(request.getReservePrice())
                .category(category)
                .seller(seller)
                .status(ItemStatus.ACTIVE)
                .build();

        AuctionItem savedItem = itemRepository.save(item);
        log.info("Created new item with id: {} by seller id: {}", savedItem.getId(), sellerId);
        return itemMapper.toResponse(savedItem);
    }

    @Override
    @Transactional
    public ItemResponse approveItem(Long itemId) {
        Long reviewerId = authenticationService.getCurrentUserId();
        User reviewer = userRepository.findById(reviewerId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (reviewer.getRole() == RoleName.USER) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

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
        Long reviewerId = authenticationService.getCurrentUserId();
        User reviewer = userRepository.findById(reviewerId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (reviewer.getRole() == RoleName.USER) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

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
        Long requesterId = authenticationService.getCurrentUserId();
        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

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
}
