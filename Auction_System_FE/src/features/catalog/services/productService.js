import { itemApi } from '../../../api/itemApi';
import { productMapper } from '../mappers/productMapper';
import { uploadImageDirect } from '../../../api/storageApi';

/**
 * Service to manage Product/Item logic, validations, and mapping orchestrations.
 */
export const productService = {
  /**
   * Fetches paginated and filtered items from backend
   * @param {Object} [params] - Query filter parameters (page, size, name, categoryId, status)
   * @returns {Promise<Object>} Page structure with mapped frontend models { content, totalPages, totalElements }
   */
  getAllItems: async (params = {}) => {
    try {
      const res = await itemApi.getItems(params);

      // Handle Spring Boot PageResponse structure
      if (res && Array.isArray(res.content)) {
        return {
          content: res.content.map(productMapper.toFrontendModel),
          totalPages: res.totalPages || 1,
          totalElements: res.totalElements || res.content.length
        };
      }

      // Fallback if backend returns list directly
      if (Array.isArray(res)) {
        return {
          content: res.map(productMapper.toFrontendModel),
          totalPages: 1,
          totalElements: res.length
        };
      }

      return { content: [], totalPages: 1, totalElements: 0 };
    } catch (err) {
      console.error("Failed to load items in Service", err);
      throw err;
    }
  },

  createItem: async (itemModel) => {
    const requestDto = productMapper.toRequestDto(itemModel);
    const formData = new FormData();
    Object.keys(requestDto).forEach((key) => {
      if (requestDto[key] !== undefined && requestDto[key] !== null) {
        formData.append(key, requestDto[key]);
      }
    });
    if (itemModel.imageFile) {
      const imageKey = await uploadImageDirect(itemModel.imageFile);
      formData.append('imageKeys', imageKey);
    }
    const rawDto = await itemApi.createWithFormData(formData);
    return productMapper.toFrontendModel(rawDto);
  },

  /**
   * Approves a pending item on the backend
   * @param {number} id - Item ID
   * @returns {Promise<Object>} Mapped approved item response model
   */
  approveItem: async (id) => {
    const rawDto = await itemApi.approve(id);
    return productMapper.toFrontendModel(rawDto);
  },

  /**
   * Rejects a pending item with a reason on the backend
   * @param {number} id - Item ID
   * @param {string} rejectionReason - Explanation details
   * @returns {Promise<Object>} Mapped rejected item response model
   */
  rejectItem: async (id, rejectionReason) => {
    const rawDto = await itemApi.reject(id, rejectionReason);
    return productMapper.toFrontendModel(rawDto);
  },

  /**
   * Updates an item on the backend
   * @param {number} id - Item ID
   * @param {Object} itemModel - Item details from frontend form
   * @returns {Promise<Object>} Updated mapped item response model
   */
  updateItem: async (id, itemModel) => {
    const requestDto = productMapper.toRequestDto(itemModel);
    const formData = new FormData();
    Object.entries(requestDto).forEach(([key, value]) => {
      if (value !== undefined && value !== null) formData.append(key, value);
    });
    if (itemModel.imageFile) formData.set('imageKey', await uploadImageDirect(itemModel.imageFile));
    const rawDto = await itemApi.update(id, formData);
    return productMapper.toFrontendModel(rawDto);
  },

  /**
   * Deletes an item from the backend
   * @param {number} id - Item ID
   */
  deleteItem: async (id) => {
    await itemApi.delete(id);
  },

  /**
   * Processes a list of items to standardize them into Frontend models
   * @param {Object[]} rawItems - Raw item list
   * @returns {Object[]} Standardized items
   */
  processItems: (rawItems) => {
    return Array.isArray(rawItems) ? rawItems.map(productMapper.toFrontendModel) : [];
  },

  /**
   * Validates if a bid amount meets the required constraints
   * @param {Object} item - Product/Item model
   * @param {number} bidAmount - Proposed bid
   * @returns {boolean} True if the bid is valid, false otherwise
   */
  validateBid: (item, bidAmount) => {
    if (!item) return false;
    const amount = parseFloat(bidAmount);
    if (isNaN(amount) || amount <= 0) return false;

    const minimumRequired = item.currentBid ? item.currentBid + 10 : item.reserve;
    return amount >= minimumRequired;
  },

  /**
   * Helper to retrieve the minimum allowed bid for an item
   * @param {Object} item - Product/Item model
   * @returns {number} Minimum bid amount
   */
  getMinimumBidRequired: (item) => {
    if (!item) return 0;
    return item.currentBid ? item.currentBid + 10 : item.reserve;
  },

  /**
   * Filters out items that are not ready for public catalog views
   * @param {Object[]} items - Product/Item list
   * @returns {Object[]} Public visible items
   */
  filterPublicItems: (items) => {
    if (!Array.isArray(items)) return [];
    return items.filter(item => item.status === 'Active' || item.status === 'Approved');
  }
};
