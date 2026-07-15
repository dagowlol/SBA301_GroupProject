import { apiRequest } from './apiInstance';

export const paymentApi = {
  /**
   * Fetches the current user's payment for a ended session
   */
  getMyPayment: (sessionId) => apiRequest(`/payments/session/${sessionId}/my`),

  /**
   * Generates a VNPay payment URL for a payment record
   */
  createVNPayUrl: (paymentId) => apiRequest(`/payments/vnpay/create-url/${paymentId}`),
};
