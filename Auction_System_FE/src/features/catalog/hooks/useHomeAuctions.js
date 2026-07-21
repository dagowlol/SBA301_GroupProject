import { useQuery } from '@tanstack/react-query';
import { sessionApi } from '../../../api/sessionApi';

const HOME_SESSION_LIMIT = 3;

const extractSessions = (response) => response?.content ?? response?.data ?? [];

export function useHomeAuctions() {
  return useQuery({
    queryKey: ['home-auctions'],
    queryFn: async ({ signal }) => {
      const [activeResponse, scheduledResponse] = await Promise.all([
        sessionApi.getSessions({ status: 'ACTIVE', size: HOME_SESSION_LIMIT, signal }),
        sessionApi.getSessions({ status: 'SCHEDULED', size: HOME_SESSION_LIMIT, signal }),
      ]);

      const active = extractSessions(activeResponse);
      const scheduled = extractSessions(scheduledResponse);
      return [...active, ...scheduled].slice(0, HOME_SESSION_LIMIT);
    },
    staleTime: 60_000,
    gcTime: 5 * 60_000,
  });
}
