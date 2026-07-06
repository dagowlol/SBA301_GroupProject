import { apiRequest } from './apiInstance';

export const userApi = {
  getAllUsers: () => apiRequest('/users', { method: 'GET' }),
  
  getUserById: (id) => apiRequest(`/users/${id}`, { method: 'GET' }),
  
  createUser: (data) => apiRequest('/users', {
    method: 'POST',
    body: JSON.stringify(data),
  }),
  
  updateUser: (id, data) => apiRequest(`/users/${id}`, {
    method: 'PUT',
    body: JSON.stringify(data),
  }),
  
  assignRole: (id, roleData) => apiRequest(`/users/${id}/role`, {
    method: 'PATCH',
    body: JSON.stringify(roleData),
  }),
  
  updateStatus: (id, statusData) => apiRequest(`/users/${id}/status`, {
    method: 'PATCH',
    body: JSON.stringify(statusData),
  }),
  
  deleteUser: (id) => apiRequest(`/users/${id}`, { method: 'DELETE' }),

  changePassword: (data) => apiRequest(`/users/password`, {
    method: 'PUT',
    body: JSON.stringify(data),
  }),
};
