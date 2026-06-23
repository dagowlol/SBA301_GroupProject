import { apiRequest } from './apiInstance';

export const auctionApi = {
  /**
   * Fetches the detail of a specific auction session.
   * @param {number|string} sessionId - Session ID
   * @returns {Promise<Object>} API response body containing the result
   */
  getDetail: (sessionId) => apiRequest(`/v1/auction-sessions/${sessionId}`),
};
