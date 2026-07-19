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
import { resolveImageUrl } from '../../../utils/imageUtils';

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
  return new Date(dateStr).toLocaleString('en-US', {
    weekday: 'long', day: '2-digit', month: '2-digit', year: 'numeric',
    hour: '2-digit', minute: '2-digit'
  });
};

const formatPrice = (amount) => {
  if (!amount && amount !== 0) return '—';
  return parseFloat(amount).toLocaleString('en-US') + ' USD';
};

// SessionStatus config
const statusConfig = {
  ACTIVE:          { bg: 'success',   label: '🔴 Live Now', textClass: 'text-success' },
  SCHEDULED:       { bg: 'warning',   label: '🗓 Upcoming', textClass: 'text-warning' },
  ENDED:           { bg: 'secondary', label: 'Ended', textClass: 'text-secondary' },
  RESERVE_NOT_MET: { bg: 'warning',   label: 'Reserve Not Met', textClass: 'text-warning' },
  CANCELLED:       { bg: 'danger',    label: 'Cancelled', textClass: 'text-danger' },
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
        setError('Failed to load auction session details. The session may not exist or has been removed.');
      })
      .finally(() => setLoading(false));
  }, [id, navigate]);

  // ── Loading ──────────────────────────────────────────────────────────────
  if (loading) {
    return (
      <Container className="py-5 text-center" style={{ minHeight: '60vh' }}>
        <Spinner animation="border" style={{ color: '#005f73', width: '3rem', height: '3rem' }} />
        <p className="text-muted mt-3">Loading auction session details...</p>
      </Container>
    );
  }

  // ── Error ────────────────────────────────────────────────────────────────
  if (error || !session) {
    return (
      <Container className="py-5">
        <Alert variant="danger" className="rounded-4">
          <h5 className="fw-bold">Auction Session Not Found</h5>
          <p className="mb-3">{error || 'This auction session does not exist or has been removed.'}</p>
          <Button variant="outline-danger" as={Link} to="/auction" size="sm">
            ← Back to List
          </Button>
        </Alert>
      </Container>
    );
  }

  const status = statusConfig[session.status] ?? { bg: 'secondary', label: session.status, textClass: 'text-secondary' };
  const isScheduled = session.status === 'SCHEDULED';
  const isEnded = session.status === 'ENDED' || session.status === 'CANCELLED' || session.status === 'RESERVE_NOT_MET';
  const hasCurrentBid = session.currentHighestBid && parseFloat(session.currentHighestBid) > 0;

  // Item info comes from session fields (itemName, itemDescription etc.)
  const itemName = session.itemName || session.item?.name || 'Auction Item';
  const itemDescription = session.itemDescription || session.item?.description || '';
  const itemImage = resolveImageUrl(session.itemImage) || getArtworkImage(itemName);

  return (
    <Container className="py-5">
      {/* Back button */}
      <Button
        variant="link"
        onClick={() => navigate('/auction')}
        className="text-decoration-none text-dark d-inline-flex align-items-center gap-1 mb-4 p-0"
      >
        <ArrowLeft size={16} />
        <span>Back to Auction List</span>
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
              <span className="text-white small">Session #{session.id}</span>
            </div>
          </div>

          {/* Extra info cards below image */}
          <div className="mt-4 d-flex gap-3">
            <Card className="flex-fill border-0 shadow-sm rounded-4 text-center p-3">
              <div className="text-muted small text-uppercase mb-1" style={{ fontSize: '0.72rem' }}>Starting Price</div>
              <div className="fw-bold" style={{ color: '#004e64', fontSize: '1.05rem' }}>
                {formatPrice(session.reservePrice)}
              </div>
            </Card>
            <Card className="flex-fill border-0 shadow-sm rounded-4 text-center p-3">
              <div className="text-muted small text-uppercase mb-1" style={{ fontSize: '0.72rem' }}>Current Price</div>
              <div className="fw-bold" style={{ color: hasCurrentBid ? '#0f9f83' : '#aaa', fontSize: '1.05rem' }}>
                {hasCurrentBid ? formatPrice(session.currentHighestBid) : 'No bids yet'}
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
            Auction Session #{session.id}
          </p>

          {/* Description */}
          {itemDescription && (
            <div className="mb-4">
              <h5 className="fw-bold text-dark mb-2 d-flex align-items-center gap-2">
                <Info size={16} /> Item Description
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
                  <div className="text-muted small" style={{ fontSize: '0.75rem' }}>Start Time</div>
                  <div className="fw-semibold text-dark small">{formatDateTime(session.startTime)}</div>
                </div>
              </div>
              <hr className="my-1" />
              <div className="d-flex align-items-center gap-3">
                <Clock size={16} className="text-muted flex-shrink-0" />
                <div>
                  <div className="text-muted small" style={{ fontSize: '0.75rem' }}>End Time</div>
                  <div className="fw-semibold text-dark small">{formatDateTime(session.endTime)}</div>
                </div>
              </div>
            </div>
          </Card>

          {/* Countdown — show only for SCHEDULED sessions with future start */}
          {isScheduled && session.startTime && new Date(session.startTime) > new Date() && (
            <div className="mb-4 p-3 rounded-4 border border-warning bg-warning bg-opacity-10 text-center">
              <p className="fw-semibold text-dark mb-2 small">⏳ Auction starts in:</p>
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
                  <h6 className="fw-bold mb-1">Auction Starting Soon</h6>
                  <p className="text-muted small mb-0">
                    This auction session has not opened yet. Please come back at the start time to participate in live bidding.
                  </p>
                </div>
              </div>
              <Alert variant="info" className="mb-0 py-2 px-3 small rounded-3">
                📅 You can set a reminder and return to this page when the auction begins.
              </Alert>
            </div>
          )}

          {isEnded && (
            <Alert variant="secondary" className="rounded-4 mb-4">
              <h6 className="fw-bold">Auction Has Ended</h6>
              <p className="mb-0 small text-muted">
                {session.status === 'CANCELLED'
                  ? 'This session has been cancelled by the administrator.'
                  : 'This session has ended. Thank you for your interest.'}
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
            Browse More Auctions
          </Button>
        </Col>
      </Row>
    </Container>
  );
}
