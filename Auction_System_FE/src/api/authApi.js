import { apiRequest } from './apiInstance';

export const authApi = {
  register: (data) => apiRequest('/v1/auth/register', {
    method: 'POST',
    body: JSON.stringify(data),
  }),

  verifyRegisterOtp: (data) => apiRequest('/v1/auth/register/verify-otp', {
    method: 'POST',
    body: JSON.stringify(data),
  }),

  login: (data) => apiRequest('/v1/auth/login', {
    method: 'POST',
    body: JSON.stringify(data),
  }),

  verifyLoginOtp: (data) => apiRequest('/v1/auth/login/verify-otp', {
    method: 'POST',
    body: JSON.stringify(data),
  }),

  resendOtp: (data) => apiRequest('/v1/auth/resend-otp', {
    method: 'POST',
    body: JSON.stringify(data),
  }),

  logout: () => apiRequest('/v1/auth/logout', {
    method: 'POST',
  }),
};
