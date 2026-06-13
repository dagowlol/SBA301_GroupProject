import { productMapper } from '../mappers/productMapper';

/**
 * Service to manage Product/Item logic, validations, and mapping orchestrations.
 */
export const productService = {
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
