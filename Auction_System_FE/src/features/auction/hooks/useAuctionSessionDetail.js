import { useQuery } from '@tanstack/react-query';
import { auctionService } from '../services/auctionService';

export function useAuctionSessionDetail(sessionId) {
  return useQuery({
    queryKey: ['sessionDetail', sessionId],
    queryFn: () => auctionService.getAuctionSessionDetail(sessionId),
    enabled: !!sessionId,
    staleTime: 60 * 1000,
  });
}
