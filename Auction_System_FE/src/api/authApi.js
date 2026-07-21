import { apiRequest } from './apiInstance';

export const authApi = {
  register: (data) => apiRequest('/auth/register', {
    method: 'POST',
    body: JSON.stringify(data),
  }),

  verifyRegisterOtp: (data) => apiRequest('/auth/register/verify-otp', {
    method: 'POST',
    body: JSON.stringify(data),
  }),

  login: (data) => apiRequest('/auth/login', {
    method: 'POST',
    body: JSON.stringify(data),
  }),

  verifyLoginOtp: (data) => apiRequest('/auth/login/verify-otp', {
    method: 'POST',
    body: JSON.stringify(data),
  }),

  resendOtp: (data) => apiRequest('/auth/resend-otp', {
    method: 'POST',
    body: JSON.stringify(data),
  }),

  logout: () => apiRequest('/auth/logout', {
    method: 'POST',
  }),

  getMe: () => apiRequest('/auth/me', {
    method: 'GET',
  }),
};
