/**
 * Mapper utility to convert Auction Session shapes between Frontend Models and Backend DTOs/WS broadcast objects.
 */
export const auctionMapper = {
  /**
   * Transforms backend session detail response into Frontend Model.
   * Supports both prompt spec fields and physical backend entity fields.
   * @param {Object} raw - Session detail response
   * @returns {Object} Frontend Auction Session Model
   */
  toSessionModel: (raw) => {
    if (!raw) return null;

    // Normalize description and bids list
    const description = raw.description || raw.itemDescription || '';
    const recentBidsRaw = raw.recentBids || raw.bidLogs || [];

    return {
      sessionId: raw.sessionId,
      itemId: raw.itemId,
      itemName: raw.itemName,
      itemImage: raw.itemImage || 'https://images.unsplash.com/photo-1579783900882-c0d3dad7b119?w=500&auto=format&fit=crop&q=60',
      description: description,
      endTime: raw.endTime,
      reservePrice: parseFloat(raw.reservePrice) || 0,
      minimumIncrement: parseFloat(raw.minimumIncrement) || 0,
      currentPrice: parseFloat(raw.currentPrice) || 0,
      currentWinnerName: raw.currentWinnerName || null,
      status: raw.status || 'ACTIVE',
      recentBids: recentBidsRaw.map((b, idx) => ({
        bidId: b.bidId || idx,
        bidderName: b.bidderName || b.bidder || 'Anonymous',
        amount: parseFloat(b.amount) || 0,
        bidTime: b.bidTime || b.time || new Date().toISOString()
      }))
    };
  },

  /**
   * Transforms WS broadcast message into a Frontend Bid Log row.
   * @param {Object} broadcastMsg - Broadcast payload from STOMP
   * @returns {Object} Frontend Bid Log Model
   */
  broadcastToBidLog: (broadcastMsg) => {
    if (!broadcastMsg) return null;
    return {
      bidId: Date.now() + Math.random(), // Unique temporary client side id
      bidderName: broadcastMsg.winnerName || 'Anonymous',
      amount: parseFloat(broadcastMsg.currentPrice) || 0,
      bidTime: broadcastMsg.bidTime || new Date().toISOString()
    };
  }
};
