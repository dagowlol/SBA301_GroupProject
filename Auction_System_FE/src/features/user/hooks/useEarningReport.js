import { useQuery, useInfiniteQuery } from '@tanstack/react-query';
import { getEarningReport, getEarningTransactions } from '../../../api/earningReportApi';

/**
 * Hook lấy tổng quan doanh thu (summary cards).
 * @param {string} userId
 */
export function useEarningSummary(userId) {
  return useQuery({
    queryKey: ['earningSummary', userId],
    queryFn: () => getEarningReport(userId),
    enabled: !!userId,
  });
}

/**
 * Hook lấy danh sách giao dịch — cursor-based pagination.
 * Pattern giống useAuctionSessions.js
 * @param {string} userId
 * @param {Object} filters - { status }
 */
export function useEarningTransactions(userId, filters = {}) {
  return useInfiniteQuery({
    queryKey: ['earningTransactions', userId, filters],
    queryFn: async ({ pageParam = null }) => {
      return await getEarningTransactions(userId, {
        ...filters,
        cursor: pageParam,
        size: 5,
      });
    },
    initialPageParam: null,
    getNextPageParam: (lastPage) => lastPage?.hasNext ? lastPage.nextCursor : undefined,
    enabled: !!userId,
  });
}
