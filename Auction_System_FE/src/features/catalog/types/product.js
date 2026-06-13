/**
 * JSDoc definitions for backend AuctionItem entity DTOs and Frontend models.
 */

/**
 * @typedef {Object} CategoryDto
 * @property {number} id
 * @property {string} name
 */

/**
 * @typedef {Object} UserDto
 * @property {number} id
 * @property {string} username
 * @property {string} name
 */

/**
 * @typedef {Object} ItemImageDto
 * @property {number} id
 * @property {string} url
 * @property {boolean} isMain
 */

/**
 * @typedef {Object} AuctionItemDto
 * @property {number} id - BaseEntity ID
 * @property {string} name - Entity item name (maps to title)
 * @property {string} description - Description
 * @property {CategoryDto} category - Linked category object
 * @property {UserDto} seller - Linked seller object (maps to submittedBy)
 * @property {number} startingPrice - Minimum starting price
 * @property {number} reservePrice - Reserve target price (maps to reserve)
 * @property {string} status - ItemStatus enum ('PENDING' | 'APPROVED' | 'ACTIVE' | 'REJECTED')
 * @property {UserDto} [reviewedBy] - Staff reviewer
 * @property {string} [reviewedAt] - Review date string
 * @property {string} [rejectionReason] - Rejection details
 * @property {ItemImageDto[]} images - Item images list
 * @property {number} [views] - Dynamic clicks tracking
 * @property {string} [startTime] - Active session start
 * @property {string} [endTime] - Active session end
 */

/**
 * @typedef {Object} FrontendProductModel
 * @property {number} id - ID
 * @property {string} title - Artwork title (maps from name)
 * @property {string} artist - Creator/seller display name (maps from seller.name)
 * @property {string} category - Category name (maps from category.name)
 * @property {number} reserve - Price threshold (maps from reservePrice)
 * @property {number|null} currentBid - Current active bid
 * @property {string} status - Display status ('Pending' | 'Approved' | 'Active' | 'Rejected')
 * @property {string} submittedBy - Seller username (maps from seller.username)
 * @property {string} image - Primary cover image URL (maps from images[0].url)
 * @property {string} description - Details
 * @property {number} views - Click views
 * @property {string} type - Timeline status ('Current' | 'Upcoming')
 * @property {string} startTime - Start time ISO
 * @property {string} endTime - End time ISO
 * @property {Object[]} bids - Historical bids list
 */

export default {};
