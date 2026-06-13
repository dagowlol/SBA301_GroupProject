/**
 * Mapper utility to convert Category shapes between Frontend Models and Backend DTOs.
 */
export const categoryMapper = {
  /**
   * Transforms CategoryResponseDto from the API into FrontendCategoryModel
   * @param {Object} dto - CategoryResponse DTO from Spring Boot
   * @returns {Object} Frontend Model
   */
  toFrontendModel: (dto) => {
    if (!dto) return null;
    return {
      id: dto.id,
      name: dto.name,
      description: dto.description || '',
      parentCategoryId: dto.parentCategoryId || null,
      parentCategoryName: dto.parentCategoryName || null,
      slug: dto.name.toLowerCase().replace(/[^a-z0-9]+/g, '-').replace(/(^-|-$)/g, ''),
      order: dto.sortOrder || 0,
      status: 'Active' // Default mock status for retrieved items
    };
  },

  /**
   * Transforms Frontend Model inputs into CategoryRequest DTO
   * @param {Object} model - Category frontend details
   * @returns {Object} CategoryRequest DTO for API request body
   */
  toRequestDto: (model) => {
    if (!model) return null;
    return {
      name: model.name,
      description: model.description || '',
      parentCategoryId: model.parentCategoryId ? parseInt(model.parentCategoryId) : null,
      sortOrder: parseInt(model.order) || 0
    };
  }
};
