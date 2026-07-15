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
import hoang.com.auction_system_be.service.auth.SecurityContextService;
import hoang.com.auction_system_be.service.item.AuctionItemServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuctionItemServiceImplTest {

    @Mock
    private AuctionItemRepository itemRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemMapper itemMapper;

    @Mock
    private SecurityContextService securityContextService;

    @InjectMocks
    private AuctionItemServiceImpl auctionItemService;

    private User seller;
    private User admin;
    private Category category;
    private AuctionItem item;
    private ItemRequest itemRequest;
    private ItemResponse itemResponse;

    @BeforeEach
    void setUp() {
        seller = User.builder()
                .id(1L)
                .role(RoleName.USER)
                .build();

        admin = User.builder()
                .id(2L)
                .role(RoleName.ADMIN)
                .build();

        category = Category.builder()
                .id(1L)
                .name("Test Category")
                .build();

        item = AuctionItem.builder()
                .id(1L)
                .name("Test Item")
                .status(ItemStatus.PENDING)
                .seller(seller)
                .category(category)
                .build();

        itemRequest = ItemRequest.builder()
                .itemName("Test Item")
                .categoryId(1L)
                .startingPrice(new BigDecimal("100"))
                .reservePrice(new BigDecimal("200"))
                .minIncrement(new BigDecimal("10"))
                .build();

        itemResponse = ItemResponse.builder()
                .id(1L)
                .name("Test Item")
                .status(ItemStatus.PENDING)
                .build();
    }

    @Test
    void createItem_Success() {
        // Arrange
        when(securityContextService.getCurrentUserEntity()).thenReturn(seller);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(itemRepository.save(any(AuctionItem.class))).thenReturn(item);
        when(itemMapper.toResponse(any(AuctionItem.class))).thenReturn(itemResponse);

        // Act
        ItemResponse result = auctionItemService.createItem(itemRequest);

        // Assert
        assertNotNull(result);
        assertEquals("Test Item", result.getName());
        verify(itemRepository, times(1)).save(any(AuctionItem.class));
    }

    @Test
    void createItem_UserNotFound_ThrowsException() {
        // Arrange
        when(securityContextService.getCurrentUserEntity()).thenThrow(new AppException(ErrorCode.USER_NOT_FOUND));

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> {
            auctionItemService.createItem(itemRequest);
        });

        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
        verify(itemRepository, never()).save(any());
    }

    @Test
    void approveItem_Success() {
        // Arrange
        when(securityContextService.checkAdminOrManagerUser()).thenReturn(admin);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemRepository.save(any(AuctionItem.class))).thenReturn(item);
        when(itemMapper.toResponse(any(AuctionItem.class))).thenReturn(itemResponse);

        // Act
        ItemResponse result = auctionItemService.approveItem(1L);

        // Assert
        assertNotNull(result);
        assertEquals(ItemStatus.APPROVED, item.getStatus());
        assertEquals(admin, item.getReviewedBy());
        verify(itemRepository, times(1)).save(item);
    }

    @Test
    void approveItem_Unauthorized_ThrowsException() {
        // Arrange
        when(securityContextService.checkAdminOrManagerUser()).thenThrow(new AppException(ErrorCode.UNAUTHORIZED));
        AppException exception = assertThrows(AppException.class, () -> {
            auctionItemService.approveItem(1L);
        });

        assertEquals(ErrorCode.UNAUTHORIZED, exception.getErrorCode());
        verify(itemRepository, never()).save(any());
    }

    @Test
    void rejectItem_Success() {
        // Arrange
        ItemRejectRequest rejectRequest = new ItemRejectRequest("Invalid item");
        when(securityContextService.checkAdminOrManagerUser()).thenReturn(admin);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemRepository.save(any(AuctionItem.class))).thenReturn(item);
        when(itemMapper.toResponse(any(AuctionItem.class))).thenReturn(itemResponse);

        // Act
        ItemResponse result = auctionItemService.rejectItem(1L, rejectRequest);

        // Assert
        assertNotNull(result);
        assertEquals(ItemStatus.REJECTED, item.getStatus());
        assertEquals("Invalid item", item.getRejectionReason());
        verify(itemRepository, times(1)).save(item);
    }

    @Test
    void getItems_Success() {
        // Arrange
        when(securityContextService.getCurrentUserEntity()).thenReturn(seller);

        Page<AuctionItem> page = new PageImpl<>(Collections.singletonList(item));
        when(itemRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(itemMapper.toResponse(any(AuctionItem.class))).thenReturn(itemResponse);

        // Act
        PageResponse<ItemResponse> result = auctionItemService.getItems(0, 10, null, null, null);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(1, result.getTotalElements());
        verify(itemRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
    }
}
