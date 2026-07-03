import { apiRequest } from './apiInstance';

export const itemApi = {
  /**
   * Fetches paginated items with filters
   */
  getItems: (params = {}) => {
    const query = new URLSearchParams();
    if (params.page !== undefined) query.append('page', params.page);
    if (params.size !== undefined) query.append('size', params.size);
    if (params.name) query.append('name', params.name);
    if (params.categoryId) query.append('categoryId', params.categoryId);
    if (params.status) query.append('status', params.status);

    const queryString = query.toString();
    const path = `/items${queryString ? `?${queryString}` : ''}`;
    return apiRequest(path);
  },

  /**
   * Submits a new item (ItemRequest DTO)
   */
  create: (itemRequestDto) => apiRequest('/items', {
    method: 'POST',
    body: JSON.stringify(itemRequestDto),
  }),

  /**
   * Submits a new item using FormData (for file uploads)
   */
  createWithFormData: (formData) => apiRequest('/items', {
    method: 'POST',
    body: formData,
  }),

  /**
   * Approves an item
   */
  approve: (id) => apiRequest(`/items/${id}/approve`, {
    method: 'PATCH',
  }),

  /**
   * Rejects an item with a reason (ItemRejectRequest DTO)
   */
  reject: (id, rejectionReason) => apiRequest(`/items/${id}/reject`, {
    method: 'PATCH',
    body: JSON.stringify({ rejectionReason }),
  }),
};
