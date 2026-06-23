package hoang.com.auction_system_be.service;

import hoang.com.auction_system_be.dto.request.CategoryRequest;
import hoang.com.auction_system_be.dto.response.CategoryResponse;
import hoang.com.auction_system_be.entity.Category;
import hoang.com.auction_system_be.exception.AppException;
import hoang.com.auction_system_be.exception.ErrorCode;
import hoang.com.auction_system_be.mapper.CategoryMapper;
import hoang.com.auction_system_be.repository.CategoryRepository;
import hoang.com.auction_system_be.service.category.CategoryServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private CategoryRequest request;
    private Category category;
    private CategoryResponse response;

    @BeforeEach
    void setUp() {
        request = CategoryRequest.builder()
                .name("Electronics")
                .description("Electronic Items")
                .sortOrder(1)
                .build();

        category = Category.builder()
                .id(1L)
                .name("Electronics")
                .description("Electronic Items")
                .sortOrder(1)
                .build();

        response = CategoryResponse.builder()
                .id(1L)
                .name("Electronics")
                .description("Electronic Items")
                .sortOrder(1)
                .build();
    }

    @Test
    void createCategory_Success() {
        // Arrange
        when(categoryRepository.findByName(anyString())).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenReturn(category);
        when(categoryMapper.toResponse(any(Category.class))).thenReturn(response);

        // Act
        CategoryResponse result = categoryService.createCategory(request);

        // Assert
        assertNotNull(result);
        assertEquals(request.getName(), result.getName());
        assertEquals(request.getDescription(), result.getDescription());
        verify(categoryRepository, times(1)).findByName(request.getName());
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    void createCategory_NameExisted_ThrowsException() {
        // Arrange
        when(categoryRepository.findByName(anyString())).thenReturn(Optional.of(category));

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> {
            categoryService.createCategory(request);
        });

        assertEquals(ErrorCode.CATEGORY_NAME_EXISTED, exception.getErrorCode());
        verify(categoryRepository, times(1)).findByName(request.getName());
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void getCategoryById_Success() {
        // Arrange
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.of(category));
        when(categoryMapper.toResponse(any(Category.class))).thenReturn(response);

        // Act
        CategoryResponse result = categoryService.getCategoryById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(category.getId(), result.getId());
        verify(categoryRepository, times(1)).findById(1L);
    }

    @Test
    void getCategoryById_NotFound_ThrowsException() {
        // Arrange
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> {
            categoryService.getCategoryById(999L);
        });

        assertEquals(ErrorCode.CATEGORY_NOT_FOUND, exception.getErrorCode());
        verify(categoryRepository, times(1)).findById(999L);
    }

    @Test
    void deleteCategory_Success() {
        // Arrange
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.of(category));
        doNothing().when(categoryRepository).delete(any(Category.class));

        // Act
        categoryService.deleteCategory(1L);

        // Assert
        verify(categoryRepository, times(1)).findById(1L);
        verify(categoryRepository, times(1)).delete(category);
    }

    @Test
    void updateCategory_Success() {
        // Arrange
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.of(category));
        when(categoryRepository.existsByNameAndIdNot(anyString(), anyLong())).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(category);
        when(categoryMapper.toResponse(any(Category.class))).thenReturn(response);

        // Act
        CategoryResponse result = categoryService.updateCategory(1L, request);

        // Assert
        assertNotNull(result);
        assertEquals(request.getName(), result.getName());
        verify(categoryRepository, times(1)).findById(1L);
        verify(categoryRepository, times(1)).existsByNameAndIdNot(request.getName(), 1L);
        verify(categoryRepository, times(1)).save(category);
    }
}
