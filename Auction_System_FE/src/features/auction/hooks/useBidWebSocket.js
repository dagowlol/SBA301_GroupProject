import { useEffect, useState, useRef } from 'react';
import { auctionSocketService } from '../services/auctionSocketService';

export function useBidWebSocket(sessionId, onLimitReached) {
  const [latestBid, setLatestBid] = useState(null);
  const [wsError, setWsError] = useState(null);
  const isSubscribedRef = useRef(false);

  useEffect(() => {
    if (!sessionId || isSubscribedRef.current) return;
    
    isSubscribedRef.current = true;
    let unsubscribe = null;

    auctionSocketService.connect(() => {
      unsubscribe = auctionSocketService.subscribeAuction(
        sessionId,
        (broadcast) => {
          setLatestBid({
            bidId: Date.now() + Math.random(),
            bidderName: broadcast.winnerName || 'Anonymous',
            amount: parseFloat(broadcast.currentPrice) || 0,
            bidTime: broadcast.bidTime || new Date().toISOString()
          });
        },
        (errorObj) => {
          setWsError(errorObj.message || 'An error occurred while placing your bid.');
        },
        (limitPayload) => {
          if (onLimitReached) {
            onLimitReached(limitPayload);
          }
        }
      );
    });

    return () => {
      if (unsubscribe) unsubscribe();
      auctionSocketService.disconnect();
      isSubscribedRef.current = false;
    };
  }, [sessionId]);

  const placeBid = (userId, amount) => {
    return auctionSocketService.sendBid(sessionId, userId, amount);
  };

  return { latestBid, wsError, placeBid, clearWsError: () => setWsError(null) };
}
