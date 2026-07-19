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
import hoang.com.auction_system_be.service.storage.ObjectStorageService;
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
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Objects;

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
    ObjectStorageService objectStorageService;

    @Override
    @Transactional
    public ItemResponse createItem(ItemRequest request) {
        User seller = securityContextService.getCurrentUserEntity();
        Long sellerId = seller.getId();

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        AuctionItem item = AuctionItem.builder()
                .name(request.getItemName())
                .description(request.getDescription())
                .startingPrice(request.getStartingPrice() != null ? request.getStartingPrice() : request.getReservePrice())
                .reservePrice(request.getReservePrice())
                .category(category)
                .seller(seller)
                .status(ItemStatus.PENDING)
                .build();

        List<String> imageKeys = new ArrayList<>();
        if (request.getImages() != null) {
            for (MultipartFile image : request.getImages()) {
                if (image != null && !image.isEmpty()) {
                    imageKeys.add(objectStorageService.uploadImage(sellerId, image));
                }
            }
        }
        if (request.getImageKeys() != null) {
            for (String objectKey : request.getImageKeys()) {
                objectStorageService.validateOwnedObject(sellerId, objectKey);
                imageKeys.add(objectKey);
            }
        }

        List<ItemImage> itemImages = new ArrayList<>();
        for (int i = 0; i < imageKeys.size(); i++) {
            itemImages.add(ItemImage.builder().item(item).imageUrl(imageKeys.get(i))
                    .isPrimary(i == 0).sortOrder(i).build());
        }
        item.setImages(itemImages);

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

        if (isTerminalItemStatus(item.getStatus())) {
            throw new AppException(ErrorCode.INVALID_ITEM_STATUS);
        }
        if (request.getStatus() != null && request.getStatus() != item.getStatus()) {
            throw new AppException(ErrorCode.INVALID_ITEM_STATUS);
        }

        boolean importantChange = hasImportantItemChange(item, request);
        if (item.getStatus() == ItemStatus.ACTIVE && importantChange) {
            throw new AppException(ErrorCode.INVALID_ITEM_STATUS);
        }
        if (item.getStatus() == ItemStatus.APPROVED && importantChange
                && item.getSessions() != null && !item.getSessions().isEmpty()) {
            throw new AppException(ErrorCode.SESSION_CONFLICT);
        }

        if (item.getStatus() == ItemStatus.PENDING || item.getStatus() == ItemStatus.REJECTED
                || (item.getStatus() == ItemStatus.APPROVED && importantChange)) {
            applyEditableItemFields(item, request);
        }
        if (request.getDescription() != null) {
            item.setDescription(request.getDescription());
        }

        if (item.getStatus() == ItemStatus.APPROVED && importantChange) {
            item.setStatus(ItemStatus.PENDING);
            item.setReviewedBy(null);
            item.setReviewedAt(null);
            item.setRejectionReason(null);
        }

        String replacementImageUrl = storeReplacementImage(request);
        if (replacementImageUrl != null) {
            item.getImages().forEach(image -> objectStorageService.deleteAfterCommit(image.getImageUrl()));
            item.getImages().clear();
            item.getImages().add(ItemImage.builder()
                    .item(item)
                    .imageUrl(replacementImageUrl)
                    .isPrimary(true)
                    .sortOrder(0)
                    .build());
        }

        AuctionItem updatedItem = itemRepository.save(item);
        log.info("Updated item with id: {}", updatedItem.getId());
        return itemMapper.toResponse(updatedItem);
    }

    private boolean hasImportantItemChange(AuctionItem item, UpdateItemRequest request) {
        return request.getName() != null && !Objects.equals(item.getName(), request.getName())
                || request.getCategoryId() != null && (item.getCategory() == null
                        || !Objects.equals(item.getCategory().getId(), request.getCategoryId()))
                || differs(item.getStartingPrice(), request.getStartingPrice())
                || differs(item.getReservePrice(), request.getReservePrice());
    }

    private boolean differs(java.math.BigDecimal current, java.math.BigDecimal requested) {
        return requested != null && (current == null || current.compareTo(requested) != 0);
    }

    private void applyEditableItemFields(AuctionItem item, UpdateItemRequest request) {
        if (request.getName() != null) item.setName(request.getName());
        if (request.getStartingPrice() != null) item.setStartingPrice(request.getStartingPrice());
        if (request.getReservePrice() != null) item.setReservePrice(request.getReservePrice());
        if (request.getCategoryId() != null) {
            item.setCategory(categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND)));
        }
    }

    private boolean isTerminalItemStatus(ItemStatus status) {
        return status == ItemStatus.SOLD || status == ItemStatus.PAID
                || status == ItemStatus.SHIPPING || status == ItemStatus.DELIVERED;
    }

    @Override
    @Transactional
    public ItemResponse updateMyItem(Long itemId, UpdateItemRequest request) {
        User currentUser = securityContextService.getCurrentUserEntity();
        AuctionItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new AppException(ErrorCode.ITEM_NOT_FOUND));

        if (item.getSeller() == null || !item.getSeller().getId().equals(currentUser.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        if (item.getStatus() != ItemStatus.PENDING && item.getStatus() != ItemStatus.REJECTED) {
            throw new AppException(ErrorCode.INVALID_ITEM_STATUS);
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
        item.setName(request.getName());
        item.setDescription(request.getDescription());
        item.setStartingPrice(request.getStartingPrice());
        item.setReservePrice(request.getReservePrice());
        item.setCategory(category);
        item.setStatus(ItemStatus.PENDING);
        item.setRejectionReason(null);
        item.setReviewedBy(null);
        item.setReviewedAt(null);

        String replacementImageUrl = storeReplacementImage(request);
        if (replacementImageUrl != null) {
            item.getImages().forEach(image -> objectStorageService.deleteAfterCommit(image.getImageUrl()));
            item.getImages().clear();
            item.getImages().add(ItemImage.builder().item(item).imageUrl(replacementImageUrl)
                    .isPrimary(true).sortOrder(0).build());
        }
        return itemMapper.toResponse(itemRepository.save(item));
    }

    private String storeReplacementImage(UpdateItemRequest request) {
        Long userId = securityContextService.getCurrentUserEntity().getId();
        if (request.getImage() != null && !request.getImage().isEmpty()) {
            return objectStorageService.uploadImage(userId, request.getImage());
        }
        if (request.getImageKey() != null && !request.getImageKey().isBlank()) {
            objectStorageService.validateOwnedObject(userId, request.getImageKey());
            return request.getImageKey();
        }
        return null;
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

    @Override
    @Transactional
    public void deleteItem(Long itemId) {
        AuctionItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new AppException(ErrorCode.ITEM_NOT_FOUND));
        if (item.getStatus() != ItemStatus.PENDING && item.getStatus() != ItemStatus.REJECTED) {
            throw new AppException(ErrorCode.INVALID_ITEM_STATUS);
        }
        item.getImages().forEach(image -> objectStorageService.deleteAfterCommit(image.getImageUrl()));
        itemRepository.delete(item);
        log.info("Deleted item with id: {}", itemId);
    }
}

