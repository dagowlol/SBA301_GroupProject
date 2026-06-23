import { auctionApi } from '../../../api/auctionApi';
import { auctionMapper } from '../mappers/auctionMapper';

/**
 * Service to manage Auction room/session operations, validation, and data mapping.
 */
export const auctionService = {
  /**
   * Fetches detailed auction session info and maps to frontend model structure
   * @param {number|string} sessionId - The session ID
   * @returns {Promise<Object>} The mapped frontend auction session object
   */
  getAuctionSessionDetail: async (sessionId) => {
    try {
      const rawDto = await auctionApi.getDetail(sessionId);
      return auctionMapper.toSessionModel(rawDto);
    } catch (err) {
      console.error(`Failed to load auction session details for ID ${sessionId} in Service:`, err);
      throw err;
    }
  }
};
