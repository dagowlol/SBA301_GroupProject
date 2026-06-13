import { categoryApi } from '../../../api/categoryApi';
import { categoryMapper } from '../mappers/categoryMapper';

/**
 * Service to manage Category operations.
 * Isolates components from raw API calls and handles data mapping.
 */
export const categoryService = {
  /**
   * Fetches all categories and maps them to Frontend Models
   * @returns {Promise<Object[]>} Mapped categories list
   */
  getAllCategories: async () => {
    const rawDtos = await categoryApi.getAll();
    return Array.isArray(rawDtos) ? rawDtos.map(categoryMapper.toFrontendModel) : [];
  },

  /**
   * Fetches a specific category by ID and maps it to Frontend Model
   * @param {number} id - Category ID
   * @returns {Promise<Object>} Mapped category object
   */
  getCategoryById: async (id) => {
    const rawDto = await categoryApi.getById(id);
    return categoryMapper.toFrontendModel(rawDto);
  },

  /**
   * Sends new Category details to backend database
   * @param {Object} categoryModel - Category input model from frontend
   * @returns {Promise<Object>} Newly created mapped category model
   */
  createCategory: async (categoryModel) => {
    const requestDto = categoryMapper.toRequestDto(categoryModel);
    const rawDto = await categoryApi.create(requestDto);
    return categoryMapper.toFrontendModel(rawDto);
  },

  /**
   * Updates existing Category details on the backend database
   * @param {number} id - Target Category ID
   * @param {Object} categoryModel - Updated Category input model
   * @returns {Promise<Object>} Updated mapped category model
   */
  updateCategory: async (id, categoryModel) => {
    const requestDto = categoryMapper.toRequestDto(categoryModel);
    const rawDto = await categoryApi.update(id, requestDto);
    return categoryMapper.toFrontendModel(rawDto);
  },

  /**
   * Soft-deletes target category from backend
   * @param {number} id - Category ID
   * @returns {Promise<Object>} Message payload
   */
  deleteCategory: async (id) => {
    return await categoryApi.delete(id);
  }
};
