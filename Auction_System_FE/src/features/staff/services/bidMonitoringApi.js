import { apiRequest } from '../../../api/apiInstance';

export const fetchBidLogs = async (page = 0, size = 50, sessionId = null, userId = null) => {
  let url = `/bids?page=${page}&size=${size}`;
  if (sessionId) url += `&sessionId=${sessionId}`;
  if (userId) url += `&userId=${userId}`;
  return await apiRequest(url, { method: 'GET' });
};
