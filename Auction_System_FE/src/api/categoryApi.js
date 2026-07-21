import { apiRequest } from './apiInstance';

export const categoryApi = {
  getAll: () => apiRequest('/categories'),
  
  getById: (id) => apiRequest(`/categories/${id}`),
  
  create: (categoryRequestDto) => apiRequest('/categories', {
    method: 'POST',
    body: JSON.stringify(categoryRequestDto),
  }),
  
  update: (id, categoryRequestDto) => apiRequest(`/categories/${id}`, {
    method: 'PUT',
    body: JSON.stringify(categoryRequestDto),
  }),
  
  delete: (id) => apiRequest(`/categories/${id}`, {
    method: 'DELETE',
  }),
};
