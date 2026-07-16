import React, { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { Spinner, Alert } from 'react-bootstrap';
import { message } from 'antd';
import { itemApi } from '../../../api/itemApi';
import { categoryApi } from '../../../api/categoryApi';
import { paymentApi } from '../../../api/paymentApi';
import { resolveImageUrl } from '../../../utils/imageUtils';
import {
  Upload, Trophy, Search, Filter, ChevronLeft, ChevronRight,
  Tag, DollarSign, Calendar, ArrowRight, Package, X, CreditCard, CheckCircle2, Clock, XCircle, RefreshCw
} from 'lucide-react';

// ── Status badge configuration ──────────────────────────────────────────────
const STATUS_CONFIG = {
  PENDING: { label: 'Pending', color: '#f59e0b', bg: 'rgba(245,158,11,0.12)' },
  APPROVED: { label: 'Approved', color: '#10b981', bg: 'rgba(16,185,129,0.12)' },
  REJECTED: { label: 'Rejected', color: '#ef4444', bg: 'rgba(239,68,68,0.12)' },
  ACTIVE: { label: 'Active', color: '#3b82f6', bg: 'rgba(59,130,246,0.12)' },
  SOLD: { label: 'Sold', color: '#8b5cf6', bg: 'rgba(139,92,246,0.12)' },
  PAID: { label: 'Paid', color: '#059669', bg: 'rgba(5,150,105,0.12)' },
  SHIPPING: { label: 'Shipping', color: '#0ea5e9', bg: 'rgba(14,165,233,0.12)' },
  DELIVERED: { label: 'Delivered', color: '#22c55e', bg: 'rgba(34,197,94,0.12)' },
};

const ITEM_STATUSES = Object.keys(STATUS_CONFIG);

const StatusBadge = ({ status }) => {
  const cfg = STATUS_CONFIG[status] || { label: status, color: '#6b7280', bg: 'rgba(107,114,128,0.12)' };
  return (
    <span style={{
      display: 'inline-flex', alignItems: 'center', gap: '4px',
      padding: '3px 10px', borderRadius: '999px', fontSize: '0.72rem',
      fontWeight: 600, letterSpacing: '0.03em',
      color: cfg.color, background: cfg.bg, border: `1px solid ${cfg.color}33`
    }}>
      <span style={{ width: 6, height: 6, borderRadius: '50%', background: cfg.color, display: 'inline-block' }} />
      {cfg.label}
    </span>
  );
};

const PlaceholderImage = ({ name = '' }) => (
  <div style={{
    width: '100%', height: '100%',
    background: 'linear-gradient(135deg, #004e6422 0%, #00b4d822 100%)',
    display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center',
    gap: 6
  }}>
    <Package size={28} color="#004e64" opacity={0.4} />
    <span style={{ fontSize: '0.65rem', color: '#004e6480', fontWeight: 500, textAlign: 'center', padding: '0 8px' }}>
      {name ? name.slice(0, 20) : 'No Image'}
    </span>
  </div>
);

const formatVND = (v) =>
  v != null ? new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(v) : '-';

const formatDate = (d) =>
  d ? new Date(d).toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric' }) : '-';

const ItemCard = ({ item, onClick }) => {
  const [imgError, setImgError] = useState(false);
  return (
    <div
      onClick={() => onClick(item)}
      style={{
        background: '#fff', borderRadius: 14, border: '1px solid #e5e7eb',
        overflow: 'hidden', cursor: 'pointer', transition: 'all 0.22s ease',
        boxShadow: '0 1px 4px rgba(0,0,0,0.05)',
      }}
      onMouseEnter={e => {
        e.currentTarget.style.transform = 'translateY(-4px)';
        e.currentTarget.style.boxShadow = '0 8px 24px rgba(0,78,100,0.13)';
        e.currentTarget.style.borderColor = '#004e6433';
      }}
      onMouseLeave={e => {
        e.currentTarget.style.transform = 'translateY(0)';
        e.currentTarget.style.boxShadow = '0 1px 4px rgba(0,0,0,0.05)';
        e.currentTarget.style.borderColor = '#e5e7eb';
      }}
    >
      <div style={{ height: 180, overflow: 'hidden', background: '#f8fafc', position: 'relative' }}>
        {resolveImageUrl(item.imageUrl) && !imgError ? (
          <img
            src={resolveImageUrl(item.imageUrl)}
            alt={item.name}
            onError={() => setImgError(true)}
            style={{ width: '100%', height: '100%', objectFit: 'cover', transition: 'transform 0.3s ease' }}
          />
        ) : (
          <PlaceholderImage name={item.name} />
        )}
        <div style={{ position: 'absolute', top: 10, right: 10 }}>
          <StatusBadge status={item.status} />
        </div>
      </div>
      <div style={{ padding: '14px 16px 16px' }}>
        <h6 style={{
          margin: 0, fontWeight: 700, fontSize: '0.9rem', color: '#111827',
          lineHeight: 1.35, display: '-webkit-box', WebkitLineClamp: 2,
          WebkitBoxOrient: 'vertical', overflow: 'hidden', marginBottom: 10,
        }}>
          {item.name}
        </h6>
        <div style={{ display: 'flex', flexDirection: 'column', gap: 6 }}>
          {item.categoryName && (
            <div style={{ display: 'flex', alignItems: 'center', gap: 5 }}>
              <Tag size={12} color="#6b7280" />
              <span style={{ fontSize: '0.75rem', color: '#6b7280' }}>{item.categoryName}</span>
            </div>
          )}
          <div style={{ display: 'flex', alignItems: 'center', gap: 5 }}>
            <DollarSign size={12} color="#004e64" />
            <span style={{ fontSize: '0.75rem', color: '#004e64', fontWeight: 600 }}>
              {formatVND(item.startingPrice)}
            </span>
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: 5 }}>
            <Calendar size={12} color="#9ca3af" />
            <span style={{ fontSize: '0.72rem', color: '#9ca3af' }}>{formatDate(item.createdAt)}</span>
          </div>
        </div>
        <div style={{
          marginTop: 12, paddingTop: 10, borderTop: '1px solid #f3f4f6',
          display: 'flex', alignItems: 'center', justifyContent: 'flex-end',
          color: '#004e64', fontSize: '0.78rem', fontWeight: 600, gap: 3,
        }}>
          View Details <ArrowRight size={13} />
        </div>
      </div>
    </div>
  );
};

// ── Payment Status Badge ─────────────────────────────────────────────────────
const PAYMENT_STATUS_CONFIG = {
  PENDING: { label: 'Chưa thanh toán', color: '#d97706', bg: 'rgba(217,119,6,0.12)', icon: Clock },
  PAID: { label: 'Đã thanh toán', color: '#059669', bg: 'rgba(5,150,105,0.12)', icon: CheckCircle2 },
  FAILED: { label: 'Thanh toán lỗi', color: '#dc2626', bg: 'rgba(220,38,38,0.12)', icon: XCircle },
  REFUNDED: { label: 'Đã hoàn tiền', color: '#7c3aed', bg: 'rgba(124,58,237,0.12)', icon: RefreshCw },
};

const PaymentStatusBadge = ({ status }) => {
  const cfg = PAYMENT_STATUS_CONFIG[status] || { label: status, color: '#6b7280', bg: 'rgba(107,114,128,0.12)', icon: Clock };
  const Icon = cfg.icon;
  return (
    <span style={{
      display: 'inline-flex', alignItems: 'center', gap: 5,
      padding: '4px 10px', borderRadius: '999px', fontSize: '0.72rem',
      fontWeight: 600, color: cfg.color, background: cfg.bg, border: `1px solid ${cfg.color}44`,
    }}>
      <Icon size={11} />
      {cfg.label}
    </span>
  );
};

// ── Won Item Card (with payment section) ─────────────────────────────────────
const WonItemCard = ({ item, payingId, onPay, onClick }) => {
  const [imgError, setImgError] = useState(false);
  const isPaying = payingId === item.sessionId;
  const paymentStatus = item.paymentStatus;   // comes from API
  const paymentId = item.paymentId;           // comes from API

  return (
    <div
      style={{
        background: '#fff', borderRadius: 14, border: '1px solid #e5e7eb',
        overflow: 'hidden', transition: 'all 0.22s ease',
        boxShadow: '0 1px 4px rgba(0,0,0,0.05)', display: 'flex', flexDirection: 'column',
      }}
      onMouseEnter={e => {
        e.currentTarget.style.boxShadow = '0 8px 24px rgba(0,78,100,0.13)';
        e.currentTarget.style.borderColor = '#004e6433';
      }}
      onMouseLeave={e => {
        e.currentTarget.style.boxShadow = '0 1px 4px rgba(0,0,0,0.05)';
        e.currentTarget.style.borderColor = '#e5e7eb';
      }}
    >
      {/* Image */}
      <div
        onClick={() => onClick(item)}
        style={{ height: 160, overflow: 'hidden', background: '#f8fafc', position: 'relative', cursor: 'pointer', flexShrink: 0 }}
      >
        {resolveImageUrl(item.imageUrl) && !imgError ? (
          <img
            src={resolveImageUrl(item.imageUrl)}
            alt={item.name}
            onError={() => setImgError(true)}
            style={{ width: '100%', height: '100%', objectFit: 'cover' }}
          />
        ) : (
          <PlaceholderImage name={item.name} />
        )}
        {/* Trophy overlay */}
        <div style={{
          position: 'absolute', top: 0, left: 0, right: 0, bottom: 0,
          background: 'linear-gradient(to top, rgba(0,0,0,0.35) 0%, transparent 50%)',
          pointerEvents: 'none',
        }} />
        <div style={{ position: 'absolute', bottom: 8, left: 10 }}>
          <Trophy size={16} color="#fbbf24" fill="#fbbf24" />
        </div>
      </div>

      {/* Body */}
      <div style={{ padding: '12px 14px', flex: 1, display: 'flex', flexDirection: 'column', gap: 8 }}>
        <h6
          onClick={() => onClick(item)}
          style={{
            margin: 0, fontWeight: 700, fontSize: '0.88rem', color: '#111827',
            lineHeight: 1.35, cursor: 'pointer',
            display: '-webkit-box', WebkitLineClamp: 2, WebkitBoxOrient: 'vertical', overflow: 'hidden',
          }}
        >
          {item.name}
        </h6>

        {/* Meta */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
          {item.categoryName && (
            <div style={{ display: 'flex', alignItems: 'center', gap: 5 }}>
              <Tag size={11} color="#6b7280" />
              <span style={{ fontSize: '0.73rem', color: '#6b7280' }}>{item.categoryName}</span>
            </div>
          )}
          <div style={{ display: 'flex', alignItems: 'center', gap: 5 }}>
            <DollarSign size={11} color="#004e64" />
            <span style={{ fontSize: '0.73rem', color: '#004e64', fontWeight: 600 }}>
              {formatVND(item.startingPrice)}
            </span>
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: 5 }}>
            <Calendar size={11} color="#9ca3af" />
            <span style={{ fontSize: '0.7rem', color: '#9ca3af' }}>{formatDate(item.createdAt)}</span>
          </div>
        </div>

        {/* Payment section */}
        <div style={{ marginTop: 'auto', paddingTop: 10, borderTop: '1px solid #f3f4f6' }}>
          {!paymentStatus ? (
            <span style={{ fontSize: '0.73rem', color: '#9ca3af', fontStyle: 'italic' }}>
              Chưa có thông tin thanh toán
            </span>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                <span style={{ fontSize: '0.72rem', color: '#6b7280', fontWeight: 600 }}>Thanh toán</span>
                <PaymentStatusBadge status={paymentStatus} />
              </div>
              {paymentStatus === 'PENDING' && paymentId && (
                <button
                  onClick={(e) => { e.stopPropagation(); onPay(paymentId, item.sessionId); }}
                  disabled={isPaying}
                  style={{
                    width: '100%', padding: '8px 12px', borderRadius: 8, border: 'none',
                    background: isPaying ? '#f3f4f6' : 'linear-gradient(135deg, #d97706 0%, #b45309 100%)',
                    color: isPaying ? '#9ca3af' : '#fff',
                    fontWeight: 600, fontSize: '0.8rem', cursor: isPaying ? 'not-allowed' : 'pointer',
                    display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 6,
                    transition: 'opacity 0.2s ease', fontFamily: 'inherit',
                  }}
                  onMouseEnter={e => { if (!isPaying) e.currentTarget.style.opacity = '0.88'; }}
                  onMouseLeave={e => { e.currentTarget.style.opacity = '1'; }}
                >
                  {isPaying
                    ? <><Spinner animation="border" size="sm" style={{ width: 13, height: 13, borderWidth: 2 }} /> Đang xử lý...</>
                    : <><CreditCard size={14} /> Thanh toán VNPay</>
                  }
                </button>
              )}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

const paginationBtnStyle = (active, disabled) => ({
  width: 34, height: 34, borderRadius: 8, border: 'none',
  background: active ? '#004e64' : '#f3f4f6',
  color: active ? '#fff' : disabled ? '#d1d5db' : '#374151',
  fontWeight: active ? 700 : 500, fontSize: '0.82rem',
  cursor: disabled ? 'not-allowed' : 'pointer',
  display: 'flex', alignItems: 'center', justifyContent: 'center',
  transition: 'all 0.15s ease', opacity: disabled ? 0.5 : 1,
});

const Pagination = ({ page, totalPages, onPageChange }) => {
  if (totalPages <= 1) return null;
  const pages = Array.from({ length: totalPages }, (_, i) => i);
  const visible = pages.filter(p => Math.abs(p - page) <= 2);
  return (
    <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 6, marginTop: 28 }}>
      <button onClick={() => onPageChange(page - 1)} disabled={page === 0} style={paginationBtnStyle(false, page === 0)}>
        <ChevronLeft size={16} />
      </button>
      {visible[0] > 0 && (
        <>
          <button onClick={() => onPageChange(0)} style={paginationBtnStyle(page === 0, false)}>1</button>
          {visible[0] > 1 && <span style={{ color: '#9ca3af' }}>...</span>}
        </>
      )}
      {visible.map(p => (
        <button key={p} onClick={() => onPageChange(p)} style={paginationBtnStyle(p === page, false)}>{p + 1}</button>
      ))}
      {visible[visible.length - 1] < totalPages - 1 && (
        <>
          {visible[visible.length - 1] < totalPages - 2 && <span style={{ color: '#9ca3af' }}>...</span>}
          <button onClick={() => onPageChange(totalPages - 1)} style={paginationBtnStyle(page === totalPages - 1, false)}>
            {totalPages}
          </button>
        </>
      )}
      <button onClick={() => onPageChange(page + 1)} disabled={page >= totalPages - 1} style={paginationBtnStyle(false, page >= totalPages - 1)}>
        <ChevronRight size={16} />
      </button>
    </div>
  );
};

export default function AuctionItemsTab() {
  const navigate = useNavigate();
  const [viewMode, setViewMode] = useState('uploaded');
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [categories, setCategories] = useState([]);
  const [filters, setFilters] = useState({ name: '', categoryId: '', status: '' });
  const [draftName, setDraftName] = useState('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const PAGE_SIZE = 8;

  // Payment state
  const [payingSessionId, setPayingSessionId] = useState(null);

  useEffect(() => {
    categoryApi.getAll().then(res => setCategories(res || [])).catch(() => { });
  }, []);

  const fetchItems = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const params = {
        page, size: PAGE_SIZE,
        name: filters.name || undefined,
        categoryId: filters.categoryId || undefined,
        status: (viewMode === 'uploaded' && filters.status) ? filters.status : undefined,
      };
      const res = viewMode === 'uploaded'
        ? await itemApi.getMyUploadedItems(params)
        : await itemApi.getMyWonItems(params);
      setItems(res.content || []);
      setTotalPages(res.totalPages || 0);
      setTotalElements(res.totalElements || 0);
    } catch (err) {
      setError(err?.message || 'Failed to load items. Please try again.');
      setItems([]);
    } finally {
      setLoading(false);
    }
  }, [viewMode, filters, page]);

  useEffect(() => { fetchItems(); }, [fetchItems]);


  const handlePayWithVNPay = async (paymentId, sessionId) => {
    setPayingSessionId(sessionId);
    try {
      const res = await paymentApi.createVNPayUrl(paymentId);
      if (res && res.url) {
        window.location.href = res.url;
      } else {
        message.error('Không nhận được đường dẫn thanh toán.');
      }
    } catch (err) {
      message.error('Có lỗi xảy ra: ' + err.message);
    } finally {
      setPayingSessionId(null);
    }
  };

  const handleViewModeChange = (mode) => {
    setViewMode(mode); setPage(0);
    setFilters({ name: '', categoryId: '', status: '' }); setDraftName('');
  };
  const handleFilterChange = (key, value) => { setFilters(prev => ({ ...prev, [key]: value })); setPage(0); };
  const handleSearchSubmit = (e) => { e.preventDefault(); handleFilterChange('name', draftName); };
  const clearFilters = () => { setFilters({ name: '', categoryId: '', status: '' }); setDraftName(''); setPage(0); };
  const hasActiveFilters = filters.name || filters.categoryId || filters.status;

  const handleItemClick = (item) => {
    if (item.sessionId) {
      navigate(`/auction/${item.sessionId}`);
    }
  };

  return (
    <div>
      <div style={{ marginBottom: 24 }}>
        <h4 style={{ fontWeight: 700, color: '#111827', margin: 0 }}>My Auction Items</h4>
        <p style={{ color: '#6b7280', fontSize: '0.875rem', marginTop: 4, marginBottom: 0 }}>
          View and manage items you have uploaded or won in auctions.
        </p>
      </div>

      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'wrap', gap: 10, marginBottom: 20 }}>
        <div style={{ display: 'inline-flex', background: '#f3f4f6', borderRadius: 10, padding: 4, gap: 2 }}>
          {[
            { id: 'uploaded', label: 'Uploaded Items', icon: Upload },
            { id: 'won', label: 'Won Items', icon: Trophy },
          ].map(({ id, label, icon: Icon }) => (
            <button key={id} onClick={() => handleViewModeChange(id)} style={{
              display: 'flex', alignItems: 'center', gap: 7, padding: '8px 18px', borderRadius: 8, border: 'none',
              background: viewMode === id ? '#004e64' : 'transparent',
              color: viewMode === id ? '#fff' : '#6b7280',
              fontWeight: viewMode === id ? 600 : 500, fontSize: '0.85rem', cursor: 'pointer',
              transition: 'all 0.2s ease',
              boxShadow: viewMode === id ? '0 2px 8px rgba(0,78,100,0.25)' : 'none',
            }}>
              <Icon size={15} />{label}
            </button>
          ))}
        </div>
        <button
          onClick={() => navigate('/user/items/create')}
          style={{
            display: 'flex', alignItems: 'center', gap: 7, padding: '8px 18px', borderRadius: 8, border: 'none',
            background: 'linear-gradient(135deg, #004e64 0%, #006e8a 100%)',
            color: '#fff', fontWeight: 600, fontSize: '0.85rem', cursor: 'pointer',
            boxShadow: '0 2px 8px rgba(0,78,100,0.25)', transition: 'opacity 0.2s ease',
            fontFamily: 'inherit',
          }}
          onMouseEnter={e => { e.currentTarget.style.opacity = '0.88'; }}
          onMouseLeave={e => { e.currentTarget.style.opacity = '1'; }}
        >
          <Upload size={15} /> Submit Item for Auction
        </button>
      </div>

      <div style={{
        background: '#f8fafc', borderRadius: 12, padding: '16px 18px', marginBottom: 20,
        border: '1px solid #e5e7eb', display: 'flex', flexWrap: 'wrap', gap: 10, alignItems: 'flex-end',
      }}>
        <form onSubmit={handleSearchSubmit} style={{ flex: '1 1 200px', minWidth: 180 }}>
          <label style={{ fontSize: '0.75rem', fontWeight: 600, color: '#6b7280', display: 'block', marginBottom: 5 }}>
            SEARCH
          </label>
          <div style={{ display: 'flex', gap: 6 }}>
            <input
              type="text" placeholder="Search by name..." value={draftName}
              onChange={e => setDraftName(e.target.value)}
              style={{ flex: 1, padding: '8px 12px', borderRadius: 8, fontSize: '0.85rem', border: '1px solid #d1d5db', outline: 'none', color: '#111827' }}
            />
            <button type="submit" style={{ padding: '8px 12px', borderRadius: 8, border: 'none', background: '#004e64', color: '#fff', cursor: 'pointer' }}>
              <Search size={14} />
            </button>
          </div>
        </form>

        <div style={{ flex: '1 1 160px', minWidth: 140 }}>
          <label style={{ fontSize: '0.75rem', fontWeight: 600, color: '#6b7280', display: 'block', marginBottom: 5 }}>CATEGORY</label>
          <select value={filters.categoryId} onChange={e => handleFilterChange('categoryId', e.target.value)}
            style={{ width: '100%', padding: '8px 12px', borderRadius: 8, fontSize: '0.85rem', border: '1px solid #d1d5db', outline: 'none', color: '#111827', background: '#fff' }}>
            <option value="">All Categories</option>
            {categories.map(cat => <option key={cat.id} value={cat.id}>{cat.name}</option>)}
          </select>
        </div>

        {viewMode === 'uploaded' && (
          <div style={{ flex: '1 1 150px', minWidth: 130 }}>
            <label style={{ fontSize: '0.75rem', fontWeight: 600, color: '#6b7280', display: 'block', marginBottom: 5 }}>
              <Filter size={12} style={{ marginRight: 4 }} />STATUS
            </label>
            <select value={filters.status} onChange={e => handleFilterChange('status', e.target.value)}
              style={{ width: '100%', padding: '8px 12px', borderRadius: 8, fontSize: '0.85rem', border: '1px solid #d1d5db', outline: 'none', color: '#111827', background: '#fff' }}>
              <option value="">All Statuses</option>
              {ITEM_STATUSES.map(s => <option key={s} value={s}>{STATUS_CONFIG[s].label}</option>)}
            </select>
          </div>
        )}

        {hasActiveFilters && (
          <button onClick={clearFilters} style={{
            alignSelf: 'flex-end', padding: '8px 14px', borderRadius: 8, border: '1px solid #e5e7eb',
            background: '#fff', color: '#6b7280', cursor: 'pointer', fontSize: '0.82rem',
            display: 'flex', alignItems: 'center', gap: 5,
          }}>
            <X size={13} /> Clear
          </button>
        )}
      </div>

      {!loading && !error && totalElements > 0 && (
        <div style={{ marginBottom: 14, fontSize: '0.82rem', color: '#6b7280' }}>
          Showing <strong style={{ color: '#111827' }}>{items.length}</strong> of <strong style={{ color: '#111827' }}>{totalElements}</strong> items
        </div>
      )}

      {error && <Alert variant="danger" style={{ borderRadius: 10 }}>{error}</Alert>}

      {loading && (
        <div style={{ textAlign: 'center', padding: '60px 0' }}>
          <Spinner animation="border" style={{ color: '#004e64', width: 36, height: 36 }} />
          <p style={{ marginTop: 12, color: '#6b7280', fontSize: '0.875rem' }}>Loading your items...</p>
        </div>
      )}

      {!loading && !error && items.length === 0 && (
        <div style={{
          textAlign: 'center', padding: '60px 20px',
          background: '#f8fafc', borderRadius: 14, border: '1px dashed #d1d5db',
        }}>
          {viewMode === 'uploaded' ? <Upload size={40} color="#d1d5db" /> : <Trophy size={40} color="#d1d5db" />}
          <p style={{ marginTop: 14, color: '#9ca3af', fontWeight: 600, fontSize: '0.95rem' }}>
            {viewMode === 'uploaded' ? 'No uploaded items yet' : 'No won items yet'}
          </p>
          <p style={{ color: '#c4c9d4', fontSize: '0.82rem', margin: 0 }}>
            {viewMode === 'uploaded'
              ? 'Items you submit for auction will appear here.'
              : 'Items you win in auctions will appear here.'}
          </p>
          {viewMode === 'uploaded' && (
            <button
              onClick={() => navigate('/user/items/create')}
              style={{
                marginTop: 16, padding: '10px 22px', borderRadius: 8, border: 'none',
                background: '#004e64', color: '#fff', fontWeight: 600, fontSize: '0.85rem', cursor: 'pointer',
              }}
            >
              + Submit an Item
            </button>
          )}
        </div>
      )}

      {!loading && !error && items.length > 0 && (
        <>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(210px, 1fr))', gap: 16 }}>
            {viewMode === 'won'
              ? items.map(item => (
                <WonItemCard
                  key={item.id}
                  item={item}
                  payingId={payingSessionId}
                  onPay={handlePayWithVNPay}
                  onClick={handleItemClick}
                />
              ))
              : items.map(item => <ItemCard key={item.id} item={item} onClick={handleItemClick} />)
            }
          </div>
          <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
        </>
      )}

      {!loading && items.length > 0 && (
        <p style={{ marginTop: 16, fontSize: '0.72rem', color: '#c4c9d4', textAlign: 'right' }}>
          * Items without an assigned session cannot be previewed yet.
        </p>
      )}
    </div>
  );
}
