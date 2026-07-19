import { apiRequest } from './apiInstance';

export function getAuctionStatistics({ from, to, signal }) {
  const params = new URLSearchParams({ from, to });
  return apiRequest(`/admin/analytics/auction-statistics?${params}`, { signal });
}
