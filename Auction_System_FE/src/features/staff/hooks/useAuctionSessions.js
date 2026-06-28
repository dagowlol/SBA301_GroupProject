import { useInfiniteQuery, useMutation, useQueryClient, useQuery } from '@tanstack/react-query';
import { sessionService } from '../services/sessionService';

export function useAuctionSessions(filters) {
  return useInfiniteQuery({
    queryKey: ['auctionSessions', filters],
    queryFn: async ({ pageParam = null }) => {
      return await sessionService.getSessions({
        ...filters,
        cursor: pageParam
      });
    },
    initialPageParam: null,
    getNextPageParam: (lastPage) => lastPage?.hasNext ? lastPage.nextCursor : undefined,
  });
}

export function useCreateSession() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ data, idempotencyKey }) => sessionService.createSession(data, idempotencyKey),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['auctionSessions'] });
    }
  });
}

export function useUpdateSession() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }) => sessionService.updateSession(id, data),
    onMutate: async ({ id, data }) => {
      // Cancel any outgoing refetches so they don't overwrite our optimistic update
      await queryClient.cancelQueries({ queryKey: ['auctionSessions'] });
      
      const queryCache = queryClient.getQueryCache().findAll({ queryKey: ['auctionSessions'] });
      const previousData = {};
      
      // Optimistically update all matching infinite queries in the cache
      queryCache.forEach(query => {
        const key = query.queryKey;
        previousData[JSON.stringify(key)] = queryClient.getQueryData(key);
        
        queryClient.setQueryData(key, (oldData) => {
          if (!oldData || !oldData.pages) return oldData;
          return {
            ...oldData,
            pages: oldData.pages.map(page => ({
              ...page,
              content: page.content.map(session => 
                session.id === id ? { ...session, ...data } : session
              )
            }))
          };
        });
      });
      
      // Return a context object with the snapshotted value
      return { previousData };
    },
    onError: (err, newSession, context) => {
      // Rollback to the previous value
      if (context?.previousData) {
        Object.entries(context.previousData).forEach(([keyString, oldData]) => {
          queryClient.setQueryData(JSON.parse(keyString), oldData);
        });
      }
    },
    onSettled: () => {
      // Always refetch after error or success to ensure backend sync
      queryClient.invalidateQueries({ queryKey: ['auctionSessions'] });
    }
  });
}

export function useDeleteSession() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id) => sessionService.deleteSession(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['auctionSessions'] });
      queryClient.invalidateQueries({ queryKey: ['deletedSessions'] });
    }
  });
}

export function useDeletedSessions() {
  return useQuery({
    queryKey: ['deletedSessions'],
    queryFn: () => sessionService.getDeletedSessions(),
  });
}

export function useRestoreSession() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id) => sessionService.restoreSession(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['auctionSessions'] });
      queryClient.invalidateQueries({ queryKey: ['deletedSessions'] });
    }
  });
}
