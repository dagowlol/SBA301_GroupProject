/**
 * JSDoc definitions for WebSocket payloads, REST responses, and Frontend auction room models.
 */

/**
 * @typedef {Object} BidLogDto
 * @property {number} bidId
 * @property {string} bidderName
 * @property {number} amount
 * @property {string} bidTime - ISO string or LocalDateTime
 */

/**
 * @typedef {Object} AuctionSessionDetailDto
 * @property {number} sessionId
 * @property {number} itemId
 * @property {string} itemName
 * @property {string} itemImage
 * @property {string} itemDescription
 * @property {string} endTime - ISO string or LocalDateTime
 * @property {number} currentPrice
 * @property {string|null} currentWinnerName
 * @property {BidLogDto[]} bidLogs
 */

/**
 * @typedef {Object} BidBroadcastDto
 * @property {number} sessionId
 * @property {number} currentPrice
 * @property {string} winnerName
 * @property {string} endTime - ISO string or LocalDateTime
 * @property {string} bidTime - ISO string or LocalDateTime
 */

/**
 * @typedef {Object} ErrorSocketDto
 * @property {string} message
 */

/**
 * @typedef {Object} FrontendBidLogModel
 * @property {number|string} bidId
 * @property {string} bidderName
 * @property {number} amount
 * @property {string} bidTime
 */

/**
 * @typedef {Object} FrontendAuctionSessionModel
 * @property {number} sessionId
 * @property {number} itemId
 * @property {string} itemName
 * @property {string} itemImage
 * @property {string} description
 * @property {string} endTime
 * @property {number} currentPrice
 * @property {string|null} currentWinnerName
 * @property {FrontendBidLogModel[]} recentBids
 */

export default {};
