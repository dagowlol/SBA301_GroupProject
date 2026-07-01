import { apiRequest } from './apiInstance';

export const sessionApi = {
  /**
   * Fetches paginated sessions with filters
   * @param {Object} params - { cursor, size, search, status }
   */
  getSessions: (params = {}) => {
    const query = new URLSearchParams();
    if (params.cursor !== undefined && params.cursor !== null) query.append('cursor', params.cursor);
    if (params.size !== undefined) query.append('size', params.size);
    if (params.search) query.append('search', params.search);
    if (params.status && params.status !== 'All') query.append('status', params.status);

    const queryString = query.toString();
    const path = `/auction-sessions${queryString ? `?${queryString}` : ''}`;
    return apiRequest(path);
  },

  /**
   * Fetches the detail of a specific auction session (Staff view).
   */
  getSessionById: (id) => apiRequest(`/auction-sessions/${id}`),

  /**
   * Creates a new auction session
   * @param {Object} data - The session request payload
   * @param {string} idempotencyKey - UUID to prevent double-submission
   */
  createSession: (data, idempotencyKey) => apiRequest('/auction-sessions', {
    method: 'POST',
    headers: {
      'Idempotency-Key': idempotencyKey
    },
    body: JSON.stringify(data),
  }),

  /**
   * Updates an existing session
   */
  updateSession: (id, data) => apiRequest(`/auction-sessions/${id}`, {
    method: 'PUT',
    body: JSON.stringify(data),
  }),

  /**
   * Soft deletes a session
   */
  deleteSession: (id) => apiRequest(`/auction-sessions/${id}`, {
    method: 'DELETE',
  }),

  /**
   * Fetches deleted sessions
   */
  getDeletedSessions: () => apiRequest('/auction-sessions/deleted'),

  /**
   * Restores a soft-deleted session
   */
  restoreSession: (id) => apiRequest(`/auction-sessions/${id}/restore`, {
    method: 'PATCH',
  }),
};
