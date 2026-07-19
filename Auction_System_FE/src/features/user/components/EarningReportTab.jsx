import React, { lazy, Suspense, useState, useContext, useMemo, useEffect } from 'react';
import { Spinner } from 'react-bootstrap';
import { DollarSign, PackageCheck, Clock, Search, Download, Inbox } from 'lucide-react';
import { AuthContext } from '../../../context/AuthContext';
import {
  useEarningStatistics,
  useEarningSummary,
  useEarningTransactions,
} from '../hooks/useEarningReport';
import { exportEarningTransactions } from '../../../api/earningReportApi';
import './EarningReportTab.css';

const EarningCharts = lazy(() => import('./EarningCharts'));

// ─── Helpers ──────────────────────────────────────────────────────────────────

/** Format a monetary amount in VND. */
const formatVND = (amount) => {
  if (amount === null || amount === undefined) return '0 VND';
  return new Intl.NumberFormat('en-US').format(amount) + ' VND';
};

/** Mask the buyer's name for privacy. */
const maskBuyerName = (fullName) => {
  if (!fullName) return '***';
  const parts = fullName.trim().split(/\s+/);
  if (parts.length <= 1) return parts[0][0] + '***';
  return parts[0] + ' ' + parts[1][0] + '***';
};

/** Format a date as dd/MM/yyyy HH:mm. */
const formatDate = (dateStr) => {
  if (!dateStr) return '—';
  const d = new Date(dateStr);
  const pad = (n) => String(n).padStart(2, '0');
  return `${pad(d.getDate())}/${pad(d.getMonth() + 1)}/${d.getFullYear()} ${pad(d.getHours())}:${pad(d.getMinutes())}`;
};

// ─── Filter tab config ────────────────────────────────────────────────────────

const FILTER_TABS = [
  { key: 'ALL', label: 'All' },
  { key: 'PENDING', label: 'Pending' },
  { key: 'SUCCESS', label: 'Success' },
];

// ─── Main Component ───────────────────────────────────────────────────────────

export default function EarningReportTab() {
  const { user, isAuthenticated, openAuthModal } = useContext(AuthContext);
  // Use the authenticated user ID, with a guest fallback for mock data.
  const userId = user?.id || 'guest';

  const [filterTab, setFilterTab] = useState('ALL');
  const [searchText, setSearchText] = useState('');
  const [pageIndex, setPageIndex] = useState(0);
  const [statisticsRange, setStatisticsRange] = useState('LAST_6_MONTHS');
  const [isExporting, setIsExporting] = useState(false);
  const [exportError, setExportError] = useState('');

  // ── Data fetching ──────────────────────────────────────────────────────────

  const {
    data: summary,
    isLoading: summaryLoading,
  } = useEarningSummary(userId);

  const {
    data: statistics,
    isLoading: statisticsLoading,
    isError: statisticsError,
  } = useEarningStatistics(userId, statisticsRange);

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

  useEffect(() => {
    setPageIndex(0);
  }, [userId, filterTab]);

  const pages = txData?.pages || [];
  const transactions = pages[pageIndex]?.content || [];

  const goToNextPage = async () => {
    if (pageIndex + 1 < pages.length) {
      setPageIndex((current) => current + 1);
      return;
    }

    if (hasNextPage) {
      const result = await fetchNextPage();
      if (result.data?.pages?.[pageIndex + 1]) {
        setPageIndex((current) => current + 1);
      }
    }
  };

  // Client-side search filter (product name)
  const filteredTransactions = useMemo(() => {
    if (!searchText.trim()) return transactions;
    const q = searchText.toLowerCase();
    return transactions.filter((t) =>
      (t.productName || '').toLowerCase().includes(q)
    );
  }, [transactions, searchText]);

  const handleExport = async () => {
    if (!isAuthenticated) {
      openAuthModal?.();
      return;
    }

    setIsExporting(true);
    setExportError('');
    try {
      const fileName = `earning-transactions-${new Date().toISOString().slice(0, 10)}.xlsx`;
      let fileHandle = null;

      if ('showSaveFilePicker' in window) {
        try {
          fileHandle = await window.showSaveFilePicker({
            suggestedName: fileName,
            types: [{
              description: 'Excel workbook',
              accept: {
                'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet': ['.xlsx'],
              },
            }],
          });
        } catch (error) {
          if (error.name === 'AbortError') return;
          // Fall back to Blob download when the API exists but is unavailable
          // in the current browser context (for example, an embedded iframe).
        }
      }

      if (fileHandle) {
        const response = await exportEarningTransactions(userId, filterTab, 'response');
        const writable = await fileHandle.createWritable();
        await response.body.pipeTo(writable);
      } else {
        const blob = await exportEarningTransactions(userId, filterTab);
        const objectUrl = URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = objectUrl;
        link.download = fileName;
        document.body.appendChild(link);
        link.click();
        link.remove();
        window.setTimeout(() => URL.revokeObjectURL(objectUrl), 1_000);
      }
    } catch (error) {
      if (error.name !== 'AbortError') {
        setExportError(error.message || 'Unable to export the earning report.');
      }
    } finally {
      setIsExporting(false);
    }
  };

  // ── Render ─────────────────────────────────────────────────────────────────

  return (

    <div className="earning-report">
      {/* ═══ Header ═══ */}
      <header className="earning-report__header">
        <div>
          <h2 className="earning-report__title">Earning Report</h2>
          <p className="earning-report__subtitle">
            Revenue statistics from successful auction sessions
          </p>
        </div>
        <div className="earning-report__actions">
          <div className="earning-report__export-control">
            <button
              className="earning-report__export-btn"
              onClick={handleExport}
              disabled={isExporting}
              type="button"
            >
              <Download size={16} />
              {isExporting ? 'Exporting...' : 'Export Report'}
            </button>
            {exportError && <span className="earning-report__export-error">{exportError}</span>}
          </div>
        </div>
      </header>

      {/* ═══ Login prompt ═══ */}
      {!isAuthenticated && (
        <div
          style={{
            background: 'linear-gradient(135deg, #eff6ff 0%, #dbeafe 100%)',
            border: '1px solid #bfdbfe',
            borderRadius: '10px',
            padding: '12px 20px',
            marginBottom: '24px',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            flexWrap: 'wrap',
            gap: '8px',
          }}
        >
          <span style={{ color: '#1e40af', fontSize: '0.85rem', fontWeight: 500 }}>
            You are viewing sample data. Please sign in to view your actual report.
          </span>
        </div>
      )}

      {/* ═══ Summary Cards (AC1) ═══ */}
      <section className="earning-summary">
        {/* Card 1: Total Revenue — Green */}
        <div className="earning-card earning-card--revenue">
          <div className="earning-card__top">
            <div>
              <div className="earning-card__label">Total Revenue</div>
              <div className="earning-card__value">
                {summaryLoading ? (
                  <Spinner animation="border" size="sm" />
                ) : (
                  <>
                    {new Intl.NumberFormat('vi-VN').format(summary?.totalRevenue || 0)}
                    <span className="earning-card__suffix">VND</span>
                  </>
                )}
              </div>
            </div>
            <div className="earning-card__icon">
              <DollarSign size={22} />
            </div>
          </div>
        </div>

        {/* Card 2: Successful Products — Blue */}
        <div className="earning-card earning-card--products">
          <div className="earning-card__top">
            <div>
              <div className="earning-card__label">Successful Products</div>
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

        {/* Card 3: Pending Amount — Orange */}
        <div className="earning-card earning-card--pending">
          <div className="earning-card__top">
            <div>
              <div className="earning-card__label">Pending Amount</div>
              <div className="earning-card__value">
                {summaryLoading ? (
                  <Spinner animation="border" size="sm" />
                ) : (
                  <>
                    {new Intl.NumberFormat('vi-VN').format(summary?.pendingAmount || 0)}
                    <span className="earning-card__suffix">VND</span>
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

      <Suspense fallback={<div className="earning-analytics-fallback">Loading charts...</div>}>
        <EarningCharts
          statistics={statistics}
          range={statisticsRange}
          onRangeChange={setStatisticsRange}
          isLoading={statisticsLoading}
          isError={statisticsError}
        />
      </Suspense>

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
            placeholder="Search product name..."
            value={searchText}
            onChange={(e) => setSearchText(e.target.value)}
          />
        </div>
      </div>

      {/* ═══ Data Table (AC2) ═══ */}
      {txStatus === 'pending' ? (
        <div className="earning-loading">
          <div className="earning-spinner" />
          <p style={{ color: '#9ca3af', fontSize: '0.85rem' }}>Loading data...</p>
        </div>
      ) : filteredTransactions.length === 0 ? (
        <div className="earning-empty">
          <div className="earning-empty__icon">
            <Inbox size={28} />
          </div>
          <div className="earning-empty__title">No transactions found</div>
          <div className="earning-empty__text">
            {filterTab !== 'ALL'
              ? 'Try switching to another tab or clearing the search filter.'
              : 'Transactions from successful auction sessions will appear here.'}
          </div>
        </div>
      ) : (
        <>
          <div className="earning-table-wrapper">
            <table className="earning-table">
              <thead>
                <tr>
                  <th className="col-left">Invoice ID</th>
                  <th className="col-left">Product Name</th>
                  <th className="col-center">End Date</th>
                  <th className="col-right">Final Price</th>
                  <th className="col-left">Buyer</th>
                  <th className="col-center">Status</th>
                </tr>
              </thead>
              <tbody>
                {filteredTransactions.map((tx) => (
                  <tr key={tx.id}>
                    <td className="col-left" data-label="Invoice ID">
                      <span className="earning-table__invoice">{tx.id}</span>
                    </td>
                    <td className="col-left" data-label="Product">
                      <span className="earning-table__product">{tx.productName}</span>
                    </td>
                    <td className="col-center" data-label="End Date">
                      <span className="earning-table__date">{formatDate(tx.sessionEndDate)}</span>
                    </td>
                    <td className="col-right" data-label="Final Price">
                      <span className="earning-table__price">{formatVND(tx.finalPrice)}</span>
                    </td>
                    <td className="col-left" data-label="Buyer">
                      <span className="earning-table__buyer">{maskBuyerName(tx.buyerName)}</span>
                    </td>
                    <td className="col-center" data-label="Status">
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

          {/* ═══ Cursor Pagination ═══ */}
          {(pages.length > 1 || pageIndex > 0 || hasNextPage) && (
            <div className="earning-load-more">
              <button
                className="earning-load-more__btn"
                onClick={() => setPageIndex((current) => current - 1)}
                disabled={pageIndex === 0 || isFetchingNextPage}
              >
                Previous
              </button>
              <span className="earning-pagination__page">Page {pageIndex + 1}</span>
              <button
                className="earning-load-more__btn"
                onClick={goToNextPage}
                disabled={isFetchingNextPage || (pageIndex + 1 >= pages.length && !hasNextPage)}
              >
                {isFetchingNextPage ? (
                  <Spinner animation="border" size="sm" />
                ) : (
                  'Next'
                )}
              </button>
            </div>
          )}
        </>
      )}
    </div>
  );
}
