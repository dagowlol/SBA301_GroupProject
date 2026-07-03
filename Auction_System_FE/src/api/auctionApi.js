import { apiRequest } from './apiInstance';

export const auctionApi = {
  /**
   * Fetches the detail of a specific auction session.
   * @param {number|string} sessionId - Session ID
   * @returns {Promise<Object>} API response body containing the result
   */
  getDetail: (sessionId) => apiRequest(`/auction-sessions/${sessionId}/detail`),

  /**
   * Get Auto-Bid configuration for a session.
   */
  getAutoBidConfig: (sessionId) => apiRequest(`/auction-sessions/${sessionId}/auto-bid`),

  /**
   * Save or toggle Auto-Bid configuration for a session.
   */
  saveAutoBidConfig: (sessionId, config) => apiRequest(`/auction-sessions/${sessionId}/auto-bid`, {
    method: 'POST',
    body: JSON.stringify(config),
  }),
};
