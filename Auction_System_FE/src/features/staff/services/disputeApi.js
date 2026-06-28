import { apiRequest } from '../../../api/apiInstance';

export const fetchDisputes = async (page = 0, size = 10, sessionId = null, raisedById = null, status = null) => {
  let url = `/disputes?page=${page}&size=${size}`;
  if (sessionId) url += `&sessionId=${sessionId}`;
  if (raisedById) url += `&raisedById=${raisedById}`;
  if (status) url += `&status=${status}`;
  return await apiRequest(url, { method: 'GET' });
};

export const resolveDispute = async (id, status, resolution) => {
  return await apiRequest(`/disputes/${id}/status`, {
    method: 'PATCH',
    body: JSON.stringify({ status, resolution }),
  });
};
