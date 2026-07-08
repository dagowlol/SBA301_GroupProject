import { apiRequest } from './apiInstance';

// ============================================================
// MOCK DATA — Xóa block này khi backend API sẵn sàng
// ============================================================
const MOCK_SUMMARY = {
  totalRevenue: 18750000,
  successfulProducts: 7,
  pendingAmount: 4200000,
};

const MOCK_TRANSACTIONS = [
  {
    id: 'INV-20260701',
    productName: 'Tranh Sơn Dầu "Hoàng Hôn Hội An"',
    sessionEndDate: '2026-07-01T18:30:00Z',
    finalPrice: 5500000,
    buyerName: 'Nguyễn Văn Anh',
    buyerEmail: 'nguyenvananh@gmail.com',
    paymentStatus: 'SUCCESS',
  },
  {
    id: 'INV-20260628',
    productName: 'Tượng Gốm Bát Tràng — Rồng Phượng',
    sessionEndDate: '2026-06-28T20:00:00Z',
    finalPrice: 3200000,
    buyerName: 'Trần Thị Bích',
    buyerEmail: 'tranthibich@yahoo.com',
    paymentStatus: 'SUCCESS',
  },
  {
    id: 'INV-20260625',
    productName: 'Bộ Sưu Tập Tem Cổ Đông Dương 1920',
    sessionEndDate: '2026-06-25T17:00:00Z',
    finalPrice: 2800000,
    buyerName: 'Phạm Quốc Cường',
    buyerEmail: 'phamquoccuong@outlook.com',
    paymentStatus: 'PENDING',
  },
  {
    id: 'INV-20260620',
    productName: 'Đồng Hồ Omega Seamaster 1965',
    sessionEndDate: '2026-06-20T19:45:00Z',
    finalPrice: 4500000,
    buyerName: 'Lê Minh Đức',
    buyerEmail: 'leminhduc@gmail.com',
    paymentStatus: 'SUCCESS',
  },
  {
    id: 'INV-20260618',
    productName: 'Bình Gốm Men Lam Huế — Thế Kỷ XIX',
    sessionEndDate: '2026-06-18T21:15:00Z',
    finalPrice: 1400000,
    buyerName: 'Hoàng Thị Ema',
    buyerEmail: 'hoangthiema@gmail.com',
    paymentStatus: 'PENDING',
  },
  {
    id: 'INV-20260615',
    productName: 'Tranh Thêu Tay XQ Đà Lạt',
    sessionEndDate: '2026-06-15T16:30:00Z',
    finalPrice: 950000,
    buyerName: 'Vũ Đình Phong',
    buyerEmail: 'vudinhphong@gmail.com',
    paymentStatus: 'SUCCESS',
  },
  {
    id: 'INV-20260610',
    productName: 'Ấm Trà Tử Sa Nghi Hưng — Cao Cấp',
    sessionEndDate: '2026-06-10T14:00:00Z',
    finalPrice: 2100000,
    buyerName: 'Đặng Văn Giang',
    buyerEmail: 'dangvangiang@yahoo.com',
    paymentStatus: 'SUCCESS',
  },
  {
    id: 'INV-20260605',
    productName: 'Quạt Lụa Cổ Vẽ Tay — Phong Cảnh',
    sessionEndDate: '2026-06-05T15:30:00Z',
    finalPrice: 750000,
    buyerName: 'Ngô Thị Hương',
    buyerEmail: 'ngothihuong@gmail.com',
    paymentStatus: 'SUCCESS',
  },
  {
    id: 'INV-20260601',
    productName: 'Bộ Ấn Chương Đá Cẩm Thạch — Triều Nguyễn',
    sessionEndDate: '2026-06-01T12:00:00Z',
    finalPrice: 6800000,
    buyerName: 'Bùi Thanh Hải',
    buyerEmail: 'buithanhai@gmail.com',
    paymentStatus: 'SUCCESS',
  },
  {
    id: 'INV-20260528',
    productName: 'Tranh Lụa Phố Cổ Hà Nội — Bùi Xuân Phái',
    sessionEndDate: '2026-05-28T18:00:00Z',
    finalPrice: 12500000,
    buyerName: 'Cao Minh Khoa',
    buyerEmail: 'caominhkhoa@outlook.com',
    paymentStatus: 'PENDING',
  },
  {
    id: 'INV-20260522',
    productName: 'Đĩa Sứ Hoa Lam Bát Tràng — Thế Kỷ XVIII',
    sessionEndDate: '2026-05-22T16:45:00Z',
    finalPrice: 3800000,
    buyerName: 'Đinh Thị Linh',
    buyerEmail: 'dinhthilinh@gmail.com',
    paymentStatus: 'SUCCESS',
  },
  {
    id: 'INV-20260515',
    productName: 'Kiếm Cổ Đại Việt — Mạ Vàng',
    sessionEndDate: '2026-05-15T20:30:00Z',
    finalPrice: 9200000,
    buyerName: 'Trịnh Quang Minh',
    buyerEmail: 'trinhquangminh@yahoo.com',
    paymentStatus: 'SUCCESS',
  },
];

// ============================================================
// Cursor-based Pagination Mock Helper
// ============================================================
function mockCursorPage(allData, cursor, size, statusFilter) {
  let filtered = [...allData];

  // Filter by status
  if (statusFilter && statusFilter !== 'ALL') {
    filtered = filtered.filter((t) => t.paymentStatus === statusFilter);
  }

  // Find start index from cursor
  let startIndex = 0;
  if (cursor) {
    const cursorIndex = filtered.findIndex((t) => t.id === cursor);
    startIndex = cursorIndex >= 0 ? cursorIndex + 1 : 0;
  }

  const pageData = filtered.slice(startIndex, startIndex + size);
  const hasNext = startIndex + size < filtered.length;
  const nextCursor = hasNext ? pageData[pageData.length - 1]?.id : null;

  return {
    content: pageData,
    hasNext,
    nextCursor,
    totalElements: filtered.length,
  };
}

// ============================================================
// API Functions — Tương thích cursor pagination của backend
// ============================================================

/**
 * Lấy tổng quan doanh thu cho seller.
 * Backend endpoint: GET /users/{userId}/earning-report
 * @param {string} userId
 */
export const getEarningReport = async (userId) => {
  try {
    return await apiRequest(`/users/${userId}/earning-report`);
  } catch {
    // Fallback mock khi backend chưa sẵn sàng
    console.warn('[EarningReport] API not available, using mock data.');
    return MOCK_SUMMARY;
  }
};

/**
 * Lấy danh sách giao dịch chi tiết cho seller (cursor-paginated).
 * Backend endpoint: GET /users/{userId}/earning-transactions?cursor=&size=&status=
 *
 * Response shape: CursorPageResponse<EarningTransaction>
 *   { content: [...], hasNext: boolean, nextCursor: string|null }
 *
 * @param {string} userId
 * @param {Object} params - { cursor, size, status }
 */
export const getEarningTransactions = async (userId, params = {}) => {
  try {
    const query = new URLSearchParams();
    if (params.cursor !== undefined && params.cursor !== null) query.append('cursor', params.cursor);
    if (params.size !== undefined) query.append('size', params.size);
    if (params.status && params.status !== 'ALL') query.append('status', params.status);

    const queryString = query.toString();
    const path = `/users/${userId}/earning-transactions${queryString ? `?${queryString}` : ''}`;
    return await apiRequest(path);
  } catch {
    // Fallback mock cursor pagination
    console.warn('[EarningTransactions] API not available, using mock data.');
    return mockCursorPage(
      MOCK_TRANSACTIONS,
      params.cursor || null,
      params.size || 5,
      params.status
    );
  }
};
