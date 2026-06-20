import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

let client = null;
let currentSubscriptions = {};

export const auctionSocketService = {
  /**
   * Connect to the WebSocket SockJS endpoint and initialize STOMP client.
   * @param {function} [onConnectCallback] - Callback when connection is established
   */
  connect: (onConnectCallback) => {
    if (client && (client.active || client.connected)) {
      console.log('STOMP client already active');
      if (client.connected && onConnectCallback) {
        onConnectCallback();
      }
      return;
    }

    const storedToken = sessionStorage.getItem('accessToken');
    const connectHeaders = storedToken ? { Authorization: `Bearer ${storedToken}` } : {};

    client = new Client({
      webSocketFactory: () => new SockJS('http://localhost:8080/ws-auction'),
      connectHeaders,
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      onConnect: (frame) => {
        console.log('STOMP Connected to live auction socket');
        // Resubscribe to existing channels if reconnected
        Object.keys(currentSubscriptions).forEach((dest) => {
          const subObj = currentSubscriptions[dest];
          console.log(`Re-subscribing to ${dest}`);
          subObj.stompSub = client.subscribe(dest, subObj.callback);
        });
        if (onConnectCallback) {
          onConnectCallback();
        }
      },
      onDisconnect: () => {
        console.log('STOMP Disconnected from live auction socket');
      },
      onStompError: (frame) => {
        console.error('STOMP Broker error: ', frame.headers['message']);
      },
      onWebSocketClose: () => {
        console.log('WebSocket connection closed');
      }
    });

    client.activate();
  },

  /**
   * Disconnect the STOMP client and clean up all subscriptions.
   */
  disconnect: () => {
    if (client) {
      // Unsubscribe all
      Object.keys(currentSubscriptions).forEach((dest) => {
        const sub = currentSubscriptions[dest].stompSub;
        if (sub) sub.unsubscribe();
      });
      currentSubscriptions = {};
      
      client.deactivate();
      client = null;
      console.log('STOMP client deactivated');
    }
  },

  /**
   * Subscribe to specific auction session notifications and private error queues.
   * @param {number|string} sessionId - The auction session ID
   * @param {function} onBidReceived - Callback triggered when a new bid is broadcasted
   * @param {function} onErrorReceived - Callback triggered when an error occurs for the user
   */
  subscribeAuction: (sessionId, onBidReceived, onErrorReceived) => {
    const topicDest = `/topic/auction/${sessionId}`;
    const errorDest = `/user/queue/errors`;

    const registerSub = (dest, callback) => {
      let stompSub = null;
      if (client && client.connected) {
        stompSub = client.subscribe(dest, callback);
      }
      currentSubscriptions[dest] = { callback, stompSub };
    };

    registerSub(topicDest, (message) => {
      try {
        const payload = JSON.parse(message.body);
        onBidReceived(payload);
      } catch (err) {
        console.error('Failed to parse websocket message:', err);
      }
    });

    registerSub(errorDest, (message) => {
      try {
        const payload = JSON.parse(message.body);
        onErrorReceived(payload);
      } catch (err) {
        console.error('Failed to parse user socket error message:', err);
      }
    });

    // Return an unsubscribe handler
    return () => {
      [topicDest, errorDest].forEach((dest) => {
        const subObj = currentSubscriptions[dest];
        if (subObj) {
          if (subObj.stompSub) {
            subObj.stompSub.unsubscribe();
          }
          delete currentSubscriptions[dest];
        }
      });
    };
  },

  /**
   * Publish a place-bid command to the STOMP message broker.
   * @param {number|string} sessionId - The session ID
   * @param {number} userId - The current user's ID
   * @param {number} bidAmount - The bid amount to place
   * @returns {boolean} True if sent successfully
   */
  sendBid: (sessionId, userId, bidAmount) => {
    if (!client || !client.connected) {
      console.error('Cannot place bid. STOMP Client is not connected.');
      return false;
    }

    client.publish({
      destination: `/app/auction/${sessionId}/place-bid`,
      body: JSON.stringify({
        userId: parseInt(userId),
        bidAmount: parseFloat(bidAmount)
      })
    });
    return true;
  }
};
