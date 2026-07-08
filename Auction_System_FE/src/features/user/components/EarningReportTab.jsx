import React, { useState, useContext, useMemo } from 'react';
import { Spinner } from 'react-bootstrap';
import { DollarSign, PackageCheck, Clock, Search, Download, Inbox } from 'lucide-react';
import { AuthContext } from '../../../context/AuthContext';
import { useEarningSummary, useEarningTransactions } from '../hooks/useEarningReport';
import './EarningReportTab.css';

// ─── Helpers ──────────────────────────────────────────────────────────────────

/** Format số tiền theo chuẩn VNĐ: 1,500,000 VNĐ */
const formatVND = (amount) => {
  if (amount === null || amount === undefined) return '0 VNĐ';
  return new Intl.NumberFormat('vi-VN').format(amount) + ' VNĐ';
};

/** Masking tên người mua: "Nguyễn Văn Anh" → "Nguyễn V***" */
const maskBuyerName = (fullName) => {
  if (!fullName) return '***';
  const parts = fullName.trim().split(/\s+/);
  if (parts.length <= 1) return parts[0][0] + '***';
  return parts[0] + ' ' + parts[1][0] + '***';
};

/** Format ngày: dd/MM/yyyy HH:mm */
const formatDate = (dateStr) => {
  if (!dateStr) return '—';
  const d = new Date(dateStr);
  const pad = (n) => String(n).padStart(2, '0');
  return `${pad(d.getDate())}/${pad(d.getMonth() + 1)}/${d.getFullYear()} ${pad(d.getHours())}:${pad(d.getMinutes())}`;
};

// ─── Filter tab config ────────────────────────────────────────────────────────

const FILTER_TABS = [
  { key: 'ALL', label: 'Tất cả' },
  { key: 'PENDING', label: 'Đang chờ' },
  { key: 'SUCCESS', label: 'Thành công' },
];

// ─── Main Component ───────────────────────────────────────────────────────────

export default function EarningReportTab() {
  const { user } = useContext(AuthContext);
  const userId = user?.id;

  const [filterTab, setFilterTab] = useState('ALL');
  const [searchText, setSearchText] = useState('');

  // ── Data fetching ──────────────────────────────────────────────────────────

  const {
    data: summary,
    isLoading: summaryLoading,
  } = useEarningSummary(userId);

  const filters = useMemo(() => ({
    status: filterTab !== 'ALL' ? filterTab : undefined,
  }), [filterTab]);

  const {
    data: txData,
    fetchNextPage,
    hasNextPage,
    isFetchingNextPage,
    status: txStatus,
  } = useEarningTransactions(userId, filters);

  // Flatten all cursor pages into a single array
  const transactions = txData?.pages.flatMap((page) => page.content) || [];

  // Client-side search filter (product name)
  const filteredTransactions = useMemo(() => {
    if (!searchText.trim()) return transactions;
    const q = searchText.toLowerCase();
    return transactions.filter((t) =>
      (t.productName || '').toLowerCase().includes(q)
    );
  }, [transactions, searchText]);

  // ── Render ─────────────────────────────────────────────────────────────────

  return (
    <div className="earning-report">
      {/* ═══ Header ═══ */}
      <header className="earning-report__header">
        <div>
          <h2 className="earning-report__title">Báo Cáo Doanh Thu</h2>
          <p className="earning-report__subtitle">
            Thống kê doanh thu từ các phiên đấu giá thành công
          </p>
        </div>
        <div className="earning-report__actions">
          <button className="earning-report__export-btn" disabled title="Sắp ra mắt">
            <Download size={16} />
            Xuất báo cáo
          </button>
        </div>
      </header>

      {/* ═══ Summary Cards (AC1) ═══ */}
      <section className="earning-summary">
        {/* Card 1: Tổng doanh thu — Green */}
        <div className="earning-card earning-card--revenue">
          <div className="earning-card__top">
            <div>
              <div className="earning-card__label">Tổng doanh thu</div>
              <div className="earning-card__value">
                {summaryLoading ? (
                  <Spinner animation="border" size="sm" />
                ) : (
                  <>
                    {new Intl.NumberFormat('vi-VN').format(summary?.totalRevenue || 0)}
                    <span className="earning-card__suffix">VNĐ</span>
                  </>
                )}
              </div>
            </div>
            <div className="earning-card__icon">
              <DollarSign size={22} />
            </div>
          </div>
        </div>

        {/* Card 2: Sản phẩm thành công — Blue */}
        <div className="earning-card earning-card--products">
          <div className="earning-card__top">
            <div>
              <div className="earning-card__label">Sản phẩm thành công</div>
              <div className="earning-card__value">
                {summaryLoading ? (
                  <Spinner animation="border" size="sm" />
                ) : (
                  summary?.successfulProducts || 0
                )}
              </div>
            </div>
            <div className="earning-card__icon">
              <PackageCheck size={22} />
            </div>
          </div>
        </div>

        {/* Card 3: Tiền chờ thanh toán — Orange */}
        <div className="earning-card earning-card--pending">
          <div className="earning-card__top">
            <div>
              <div className="earning-card__label">Tiền chờ thanh toán</div>
              <div className="earning-card__value">
                {summaryLoading ? (
                  <Spinner animation="border" size="sm" />
                ) : (
                  <>
                    {new Intl.NumberFormat('vi-VN').format(summary?.pendingAmount || 0)}
                    <span className="earning-card__suffix">VNĐ</span>
                  </>
                )}
              </div>
            </div>
            <div className="earning-card__icon">
              <Clock size={22} />
            </div>
          </div>
        </div>
      </section>

      {/* ═══ Filter Bar ═══ */}
      <div className="earning-filters">
        <div className="earning-filters__tabs">
          {FILTER_TABS.map((tab) => (
            <button
              key={tab.key}
              className={`earning-filters__tab ${filterTab === tab.key ? 'earning-filters__tab--active' : ''}`}
              onClick={() => {
                setFilterTab(tab.key);
                setSearchText('');
              }}
            >
              {tab.label}
            </button>
          ))}
        </div>

        <div className="earning-filters__search">
          <Search size={16} className="earning-filters__search-icon" />
          <input
            type="text"
            className="earning-filters__search-input"
            placeholder="Tìm tên sản phẩm..."
            value={searchText}
            onChange={(e) => setSearchText(e.target.value)}
          />
        </div>
      </div>

      {/* ═══ Data Table (AC2) ═══ */}
      {txStatus === 'pending' ? (
        <div className="earning-loading">
          <div className="earning-spinner" />
          <p style={{ color: '#9ca3af', fontSize: '0.85rem' }}>Đang tải dữ liệu...</p>
        </div>
      ) : filteredTransactions.length === 0 ? (
        <div className="earning-empty">
          <div className="earning-empty__icon">
            <Inbox size={28} />
          </div>
          <div className="earning-empty__title">Không có giao dịch nào</div>
          <div className="earning-empty__text">
            {filterTab !== 'ALL'
              ? 'Thử chuyển sang tab khác hoặc xóa bộ lọc tìm kiếm.'
              : 'Các giao dịch từ phiên đấu giá thành công sẽ hiển thị tại đây.'}
          </div>
        </div>
      ) : (
        <>
          <div className="earning-table-wrapper">
            <table className="earning-table">
              <thead>
                <tr>
                  <th className="col-left">Mã hóa đơn</th>
                  <th className="col-left">Tên sản phẩm</th>
                  <th className="col-center">Ngày kết thúc</th>
                  <th className="col-right">Giá chốt cuối cùng</th>
                  <th className="col-left">Người mua</th>
                  <th className="col-center">Trạng thái</th>
                </tr>
              </thead>
              <tbody>
                {filteredTransactions.map((tx) => (
                  <tr key={tx.id}>
                    <td className="col-left" data-label="Mã HĐ">
                      <span className="earning-table__invoice">{tx.id}</span>
                    </td>
                    <td className="col-left" data-label="Sản phẩm">
                      <span className="earning-table__product">{tx.productName}</span>
                    </td>
                    <td className="col-center" data-label="Ngày KT">
                      <span className="earning-table__date">{formatDate(tx.sessionEndDate)}</span>
                    </td>
                    <td className="col-right" data-label="Giá chốt">
                      <span className="earning-table__price">{formatVND(tx.finalPrice)}</span>
                    </td>
                    <td className="col-left" data-label="Người mua">
                      <span className="earning-table__buyer">{maskBuyerName(tx.buyerName)}</span>
                    </td>
                    <td className="col-center" data-label="Trạng thái">
                      {tx.paymentStatus === 'SUCCESS' ? (
                        <span className="earning-badge earning-badge--success">
                          <span className="earning-badge__dot" />
                          SUCCESS
                        </span>
                      ) : (
                        <span className="earning-badge earning-badge--pending">
                          <span className="earning-badge__dot" />
                          PENDING
                        </span>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          {/* ═══ Load More — Cursor Pagination ═══ */}
          {hasNextPage && (
            <div className="earning-load-more">
              <button
                className="earning-load-more__btn"
                onClick={() => fetchNextPage()}
                disabled={isFetchingNextPage}
              >
                {isFetchingNextPage ? (
                  <Spinner animation="border" size="sm" />
                ) : (
                  'Tải thêm'
                )}
              </button>
            </div>
          )}
        </>
      )}
    </div>
  );
}
