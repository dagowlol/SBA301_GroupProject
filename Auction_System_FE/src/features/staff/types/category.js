/**
 * JSDoc definitions for Category entities.
 */

/**
 * @typedef {Object} CategoryRequestDto
 * @property {string} name - Category name (max 100 characters)
 * @property {string} [description] - Optional description
 * @property {number|null} [parentCategoryId] - Optional parent category identifier
 * @property {number} sortOrder - Sort ordering weight (>= 0)
 */

/**
 * @typedef {Object} CategoryResponseDto
 * @property {number} id - Category identifier
 * @property {string} name - Name
 * @property {string} [description] - Description
 * @property {number|null} [parentCategoryId] - Parent Category ID
 * @property {string|null} [parentCategoryName] - Parent Category Name
 * @property {number} sortOrder - Sort ordering weight
 * @property {string} createdAt - Date ISO string
 * @property {string} updatedAt - Date ISO string
 */

/**
 * @typedef {Object} FrontendCategoryModel
 * @property {number} id - Internal frontend category identifier
 * @property {string} name - Display name
 * @property {string} description - Brief details
 * @property {number|null} parentCategoryId - Parent category ID
 * @property {string|null} parentCategoryName - Parent category Name
 * @property {string} slug - String URL slug
 * @property {number} order - Ordering sequence index (maps to sortOrder)
 * @property {string} status - Mock status state ('Active' | 'Inactive')
 */

export default {};
