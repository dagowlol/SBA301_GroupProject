import { apiRequest } from './apiInstance';

// ============================================================
// MOCK DATA — Used for guest users
// ============================================================
const MOCK_SUMMARY = {
  totalRevenue: 18750000,
  successfulProducts: 7,
  pendingAmount: 4200000,
};

const MOCK_STATISTICS = {
  range: 'LAST_6_MONTHS',
  revenueByPeriod: [
    { period: '2026-02', revenue: 1200000 },
    { period: '2026-03', revenue: 2800000 },
    { period: '2026-04', revenue: 1750000 },
    { period: '2026-05', revenue: 3900000 },
    { period: '2026-06', revenue: 4100000 },
    { period: '2026-07', revenue: 5000000 },
  ],
  paidCount: 7,
  pendingCount: 2,
};

const MOCK_TRANSACTIONS = [
  {
    id: 'INV-20260701',
    productName: 'Oil Painting "Hoi An Sunset"',
    sessionEndDate: '2026-07-01T18:30:00Z',
    finalPrice: 5500000,
    buyerName: 'Nguyễn Văn Anh',
    buyerEmail: 'nguyenvananh@gmail.com',
    paymentStatus: 'SUCCESS',
  },
  {
    id: 'INV-20260628',
    productName: 'Bat Trang Ceramic Statue — Dragon and Phoenix',
    sessionEndDate: '2026-06-28T20:00:00Z',
    finalPrice: 3200000,
    buyerName: 'Trần Thị Bích',
    buyerEmail: 'tranthibich@yahoo.com',
    paymentStatus: 'SUCCESS',
  },
  {
    id: 'INV-20260625',
    productName: 'Indochina Vintage Stamp Collection 1920',
    sessionEndDate: '2026-06-25T17:00:00Z',
    finalPrice: 2800000,
    buyerName: 'Phạm Quốc Cường',
    buyerEmail: 'phamquoccuong@outlook.com',
    paymentStatus: 'PENDING',
  },
  {
    id: 'INV-20260620',
    productName: 'Omega Seamaster Watch 1965',
    sessionEndDate: '2026-06-20T19:45:00Z',
    finalPrice: 4500000,
    buyerName: 'Lê Minh Đức',
    buyerEmail: 'leminhduc@gmail.com',
    paymentStatus: 'SUCCESS',
  },
  {
    id: 'INV-20260618',
    productName: 'Hue Blue-and-White Ceramic Vase — 19th Century',
    sessionEndDate: '2026-06-18T21:15:00Z',
    finalPrice: 1400000,
    buyerName: 'Hoàng Thị Ema',
    buyerEmail: 'hoangthiema@gmail.com',
    paymentStatus: 'PENDING',
  },
  {
    id: 'INV-20260615',
    productName: 'XQ Da Lat Hand Embroidery',
    sessionEndDate: '2026-06-15T16:30:00Z',
    finalPrice: 950000,
    buyerName: 'Vũ Đình Phong',
    buyerEmail: 'vudinhphong@gmail.com',
    paymentStatus: 'SUCCESS',
  },
];

function mockCursorPage(allData, cursor, size, statusFilter) {
  let filtered = [...allData];
  if (statusFilter && statusFilter !== 'ALL') {
    filtered = filtered.filter((t) => t.paymentStatus === statusFilter);
  }
  let startIndex = 0;
  if (cursor) {
    const cursorIndex = filtered.findIndex((t) => t.id === cursor);
    startIndex = cursorIndex >= 0 ? cursorIndex + 1 : 0;
  }
  const pageData = filtered.slice(startIndex, startIndex + size);
  const hasNext = startIndex + size < filtered.length;
  return {
    content: pageData,
    hasNext,
    nextCursor: hasNext ? pageData[pageData.length - 1]?.id : null,
  };
}

/**
 * Get earning summary for seller.
 * Backend endpoint: GET /users/{userId}/earning-report
 * @param {string} userId
 */
export const getEarningReport = async (userId) => {
  if (userId === 'guest') return MOCK_SUMMARY;
  return await apiRequest(`/users/${userId}/earning-report`);
};

export const getEarningStatistics = async (userId, range = 'LAST_6_MONTHS') => {
  if (userId === 'guest') return { ...MOCK_STATISTICS, range };
  return await apiRequest(`/users/${userId}/earning-statistics?range=${encodeURIComponent(range)}`);
};

/**
 * Get earning transactions for seller (cursor-paginated).
 * Backend endpoint: GET /users/{userId}/earning-transactions?cursor=&size=&status=
 *
 * Response shape: CursorPageResponse<EarningTransaction>
 *   { content: [...], hasNext: boolean, nextCursor: string|null }
 *
 * @param {string} userId
 * @param {Object} params - { cursor, size, status }
 */
export const getEarningTransactions = async (userId, params = {}) => {
  if (userId === 'guest') {
    return mockCursorPage(MOCK_TRANSACTIONS, params.cursor || null, params.size || 5, params.status);
  }

  const query = new URLSearchParams();
  if (params.cursor !== undefined && params.cursor !== null) query.append('cursor', params.cursor);
  if (params.size !== undefined) query.append('size', params.size);
  if (params.status && params.status !== 'ALL') query.append('status', params.status);

  const queryString = query.toString();
  const path = `/users/${userId}/earning-transactions${queryString ? `?${queryString}` : ''}`;
  return await apiRequest(path);
};

export const exportEarningTransactions = async (userId, status = 'ALL', responseType = 'blob') => {
  const query = new URLSearchParams();
  if (status && status !== 'ALL') query.append('status', status);

  const queryString = query.toString();
  const path = `/users/${userId}/earning-transactions/export${queryString ? `?${queryString}` : ''}`;
  return await apiRequest(path, { responseType });
};
