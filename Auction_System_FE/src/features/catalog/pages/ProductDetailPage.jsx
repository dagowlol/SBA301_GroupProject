import { useEffect, useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import {
  Container, Row, Col, Button, Alert, Card, Badge, Spinner
} from 'react-bootstrap';
import {
  ArrowLeft, Clock, Hammer, CalendarClock, Tag, DollarSign, Info, Flame
} from 'lucide-react';
import { sessionApi } from '../../../api/sessionApi';
import CountdownTimer from '../../auction/components/CountdownTimer';

/**
 * ProductDetailPage — Preview page for a SCHEDULED auction session.
 *
 * Route: /product/:id  (where :id is a Session ID)
 *
 * Fetches the session detail (staff endpoint) which returns item info,
 * session timing, and pricing — then displays a rich preview.
 *
 * If the session is ACTIVE, the user is redirected to the live room.
 */

// Placeholder image based on item name
const getArtworkImage = (name = '') => {
  const n = name.toLowerCase();
  if (n.includes('mona') || n.includes('lisa')) return 'https://images.unsplash.com/photo-1580136579312-94651dfd596d?w=800&auto=format&fit=crop&q=80';
  if (n.includes('pot') || n.includes('ceramic')) return 'https://images.unsplash.com/photo-1612196808214-b8e1d6145a8c?w=800&auto=format&fit=crop&q=80';
  if (n.includes('bird') || n.includes('forest')) return 'https://images.unsplash.com/photo-1448375240586-882707db888b?w=800&auto=format&fit=crop&q=80';
  if (n.includes('apollo') || n.includes('sculpture')) return 'https://images.unsplash.com/photo-1605721911519-3dfeb3be25e7?w=800&auto=format&fit=crop&q=80';
  if (n.includes('blue') || n.includes('paint')) return 'https://images.unsplash.com/photo-1541701494587-cb58502866ab?w=800&auto=format&fit=crop&q=80';
  return 'https://images.unsplash.com/photo-1579783900882-c0d3dad7b119?w=800&auto=format&fit=crop&q=80';
};

const formatDateTime = (dateStr) => {
  if (!dateStr) return '—';
  return new Date(dateStr).toLocaleString('vi-VN', {
    weekday: 'long', day: '2-digit', month: '2-digit', year: 'numeric',
    hour: '2-digit', minute: '2-digit'
  });
};

const formatPrice = (amount) => {
  if (!amount && amount !== 0) return '—';
  return parseFloat(amount).toLocaleString('vi-VN') + ' VNĐ';
};

// SessionStatus config
const statusConfig = {
  ACTIVE:    { bg: 'success',   label: '🔴 Đang Diễn Ra', textClass: 'text-success' },
  SCHEDULED: { bg: 'warning',   label: '🗓 Sắp Diễn Ra', textClass: 'text-warning' },
  ENDED:     { bg: 'secondary', label: 'Đã Kết Thúc', textClass: 'text-secondary' },
  CANCELLED: { bg: 'danger',    label: 'Đã Hủy', textClass: 'text-danger' },
};

export default function ProductDetailPage() {
  const { id } = useParams(); // This is a Session ID
  const navigate = useNavigate();

  const [session, setSession] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (!id) return;
    setLoading(true);
    setError(null);

    // Use getSessionById (returns AuctionSessionResponse with full fields)
    // If session is ACTIVE → redirect immediately to the live auction room
    sessionApi.getSessionById(id)
      .then((data) => {
        if (data?.status === 'ACTIVE') {
          // Redirect to live room
          navigate(`/auction/${id}`, { replace: true });
          return;
        }
        setSession(data);
      })
      .catch((err) => {
        console.error('Failed to load session:', err);
        setError('Không thể tải thông tin phiên đấu giá. Phiên có thể không tồn tại hoặc đã bị xóa.');
      })
      .finally(() => setLoading(false));
  }, [id, navigate]);

  // ── Loading ──────────────────────────────────────────────────────────────
  if (loading) {
    return (
      <Container className="py-5 text-center" style={{ minHeight: '60vh' }}>
        <Spinner animation="border" style={{ color: '#005f73', width: '3rem', height: '3rem' }} />
        <p className="text-muted mt-3">Đang tải thông tin phiên đấu giá...</p>
      </Container>
    );
  }

  // ── Error ────────────────────────────────────────────────────────────────
  if (error || !session) {
    return (
      <Container className="py-5">
        <Alert variant="danger" className="rounded-4">
          <h5 className="fw-bold">Không tìm thấy phiên đấu giá</h5>
          <p className="mb-3">{error || 'Phiên đấu giá này không tồn tại hoặc đã bị xóa.'}</p>
          <Button variant="outline-danger" as={Link} to="/auction" size="sm">
            ← Quay về danh sách
          </Button>
        </Alert>
      </Container>
    );
  }

  const status = statusConfig[session.status] ?? { bg: 'secondary', label: session.status, textClass: 'text-secondary' };
  const isScheduled = session.status === 'SCHEDULED';
  const isEnded = session.status === 'ENDED' || session.status === 'CANCELLED';
  const hasCurrentBid = session.currentHighestBid && parseFloat(session.currentHighestBid) > 0;

  // Item info comes from session fields (itemName, itemDescription etc.)
  const itemName = session.itemName || session.item?.name || 'Sản phẩm đấu giá';
  const itemDescription = session.itemDescription || session.item?.description || '';
  const itemImage = session.itemImage || getArtworkImage(itemName);

  return (
    <Container className="py-5">
      {/* Back button */}
      <Button
        variant="link"
        onClick={() => navigate('/auction')}
        className="text-decoration-none text-dark d-inline-flex align-items-center gap-1 mb-4 p-0"
      >
        <ArrowLeft size={16} />
        <span>Quay về danh sách đấu giá</span>
      </Button>

      <Row className="g-5">
        {/* ── Left: Image ────────────────────────────────────────────────── */}
        <Col lg={6}>
          <div className="border rounded-4 bg-white overflow-hidden shadow-sm position-relative">
            <img
              src={itemImage}
              alt={itemName}
              className="w-100 object-fit-cover"
              style={{ maxHeight: '480px', objectFit: 'cover' }}
            />
            {/* Status overlay at bottom */}
            <div
              className="position-absolute bottom-0 start-0 w-100 d-flex align-items-center gap-2 px-4 py-2"
              style={{ background: 'linear-gradient(transparent, rgba(0,0,0,0.55))' }}
            >
              <Badge bg={status.bg} className="px-2 py-1 text-uppercase" style={{ fontSize: '0.78rem' }}>
                {status.label}
              </Badge>
              <span className="text-white small">Phiên #{session.id}</span>
            </div>
          </div>

          {/* Extra info cards below image */}
          <div className="mt-4 d-flex gap-3">
            <Card className="flex-fill border-0 shadow-sm rounded-4 text-center p-3">
              <div className="text-muted small text-uppercase mb-1" style={{ fontSize: '0.72rem' }}>Giá khởi điểm</div>
              <div className="fw-bold" style={{ color: '#004e64', fontSize: '1.05rem' }}>
                {formatPrice(session.reservePrice)}
              </div>
            </Card>
            <Card className="flex-fill border-0 shadow-sm rounded-4 text-center p-3">
              <div className="text-muted small text-uppercase mb-1" style={{ fontSize: '0.72rem' }}>Giá hiện tại</div>
              <div className="fw-bold" style={{ color: hasCurrentBid ? '#0f9f83' : '#aaa', fontSize: '1.05rem' }}>
                {hasCurrentBid ? formatPrice(session.currentHighestBid) : 'Chưa có đặt giá'}
              </div>
            </Card>
          </div>
        </Col>

        {/* ── Right: Session Info ─────────────────────────────────────────── */}
        <Col lg={6} className="text-start">
          {/* Title */}
          <h1 className="fw-bold text-dark mb-1" style={{ fontSize: '1.9rem', lineHeight: '1.25' }}>
            {itemName}
          </h1>
          <p className="text-muted mb-4" style={{ fontSize: '0.9rem' }}>
            Phiên đấu giá #{session.id}
          </p>

          {/* Description */}
          {itemDescription && (
            <div className="mb-4">
              <h5 className="fw-bold text-dark mb-2 d-flex align-items-center gap-2">
                <Info size={16} /> Mô tả sản phẩm
              </h5>
              <p className="text-muted" style={{ lineHeight: '1.7', fontSize: '0.92rem' }}>
                {itemDescription}
              </p>
            </div>
          )}

          {/* Timing Info */}
          <Card className="border-0 bg-light rounded-4 mb-4 p-3">
            <div className="d-flex flex-column gap-2">
              <div className="d-flex align-items-center gap-3">
                <CalendarClock size={16} className="text-muted flex-shrink-0" />
                <div>
                  <div className="text-muted small" style={{ fontSize: '0.75rem' }}>Thời gian bắt đầu</div>
                  <div className="fw-semibold text-dark small">{formatDateTime(session.startTime)}</div>
                </div>
              </div>
              <hr className="my-1" />
              <div className="d-flex align-items-center gap-3">
                <Clock size={16} className="text-muted flex-shrink-0" />
                <div>
                  <div className="text-muted small" style={{ fontSize: '0.75rem' }}>Thời gian kết thúc</div>
                  <div className="fw-semibold text-dark small">{formatDateTime(session.endTime)}</div>
                </div>
              </div>
            </div>
          </Card>

          {/* Countdown — show only for SCHEDULED sessions with future start */}
          {isScheduled && session.startTime && new Date(session.startTime) > new Date() && (
            <div className="mb-4 p-3 rounded-4 border border-warning bg-warning bg-opacity-10 text-center">
              <p className="fw-semibold text-dark mb-2 small">⏳ Phiên đấu giá bắt đầu sau:</p>
              <CountdownTimer
                endTime={session.startTime}
                onTimeUp={() => {
                  // Refresh page when countdown reaches 0
                  setTimeout(() => window.location.reload(), 1000);
                }}
              />
            </div>
          )}

          {/* CTA Panel */}
          {isScheduled && (
            <div className="border rounded-4 p-4 bg-white shadow-sm mb-4">
              <div className="d-flex align-items-start gap-3 mb-3">
                <Hammer size={20} className="text-muted mt-1 flex-shrink-0" />
                <div>
                  <h6 className="fw-bold mb-1">Sắp bắt đầu đấu giá</h6>
                  <p className="text-muted small mb-0">
                    Phiên đấu giá này chưa mở. Vui lòng quay lại vào thời điểm bắt đầu để tham gia đặt giá trực tiếp.
                  </p>
                </div>
              </div>
              <Alert variant="info" className="mb-0 py-2 px-3 small rounded-3">
                📅 Bạn có thể đặt lịch nhắc nhở và quay lại trang này khi đến giờ đấu giá.
              </Alert>
            </div>
          )}

          {isEnded && (
            <Alert variant="secondary" className="rounded-4 mb-4">
              <h6 className="fw-bold">Phiên đấu giá đã kết thúc</h6>
              <p className="mb-0 small text-muted">
                {session.status === 'CANCELLED'
                  ? 'Phiên này đã bị hủy bởi ban quản lý.'
                  : 'Phiên này đã kết thúc. Cảm ơn bạn đã quan tâm.'}
              </p>
            </Alert>
          )}

          {/* Back button */}
          <Button
            variant="outline-dark"
            as={Link}
            to="/auction"
            className="d-flex align-items-center gap-2 rounded-3"
          >
            <ArrowLeft size={16} />
            Xem thêm phiên đấu giá khác
          </Button>
        </Col>
      </Row>
    </Container>
  );
}
