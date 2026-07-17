import { useQuery, useInfiniteQuery } from '@tanstack/react-query';
import {
  getEarningReport,
  getEarningStatistics,
  getEarningTransactions,
} from '../../../api/earningReportApi';


export function useEarningSummary(userId) {
  return useQuery({
    queryKey: ['earningSummary', userId],
    queryFn: () => getEarningReport(userId),
    enabled: !!userId,
  });
}

export function useEarningStatistics(userId, range) {
  return useQuery({
    queryKey: ['earningStatistics', userId, range],
    queryFn: () => getEarningStatistics(userId, range),
    enabled: !!userId,
  });
}


export function useEarningTransactions(userId, filters = {}) {
  return useInfiniteQuery({
    queryKey: ['earningTransactions', userId, filters],
    queryFn: async ({ pageParam = null }) => {
      return await getEarningTransactions(userId, {
        ...filters,
        cursor: pageParam,
        size: 10,
      });
    },
    initialPageParam: null,
    getNextPageParam: (lastPage) => lastPage?.hasNext ? lastPage.nextCursor : undefined,
    enabled: !!userId,
  });
}
