package hoang.com.auction_system_be.mapper;

import hoang.com.auction_system_be.dto.response.CategoryResponse;
import hoang.com.auction_system_be.entity.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public CategoryResponse toResponse(Category category) {
        CategoryResponse categoryResponse = CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .sortOrder(category.getSortOrder())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();

        if (category.getParentCategory() != null) {
            categoryResponse.setParentCategoryId(category.getParentCategory().getId());
            categoryResponse.setParentCategoryName(category.getParentCategory().getName());
        }

        return categoryResponse;
    }
}
