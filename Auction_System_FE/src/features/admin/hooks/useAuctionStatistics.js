import { keepPreviousData, useQuery } from '@tanstack/react-query';
import { getAuctionStatistics } from '../../../api/analyticsApi';

export function useAuctionStatistics(range) {
  return useQuery({
    queryKey: ['auction-statistics', range.from, range.to],
    queryFn: ({ signal }) => getAuctionStatistics({ ...range, signal }),
    enabled: Boolean(range.from && range.to),
    placeholderData: keepPreviousData,
  });
}
