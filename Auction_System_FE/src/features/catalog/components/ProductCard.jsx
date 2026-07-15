import { Card, Badge, Button } from 'react-bootstrap';
import { useNavigate } from 'react-router-dom';
import { Clock, Flame, CalendarClock, ArrowRight } from 'lucide-react';
import { resolveImageUrl } from '../../../utils/imageUtils';

/**
 * ProductCard — Displays a single auction session.
 *
 * Props:
 *   session: AuctionSessionListResponse {
 *     id, itemId, itemName, itemDescription,
 *     reservePrice, currentHighestBid,
 *     status (SCHEDULED | ACTIVE | ENDED | CANCELLED),
 *     startTime, endTime
 *   }
 *
 * Navigation:
 *   ACTIVE   → /auction/{session.id}   (live auction room)
 *   SCHEDULED → /product/{session.id}  (session preview/detail page)
 */

// Generate a placeholder artwork image based on item name
const getArtworkImage = (name = '') => {
  const n = name.toLowerCase();
  if (n.includes('mona') || n.includes('lisa')) return 'https://images.unsplash.com/photo-1580136579312-94651dfd596d?w=500&auto=format&fit=crop&q=60';
  if (n.includes('pot') || n.includes('ceramic')) return 'https://images.unsplash.com/photo-1612196808214-b8e1d6145a8c?w=500&auto=format&fit=crop&q=60';
  if (n.includes('bird') || n.includes('forest')) return 'https://images.unsplash.com/photo-1448375240586-882707db888b?w=500&auto=format&fit=crop&q=60';
  if (n.includes('apollo') || n.includes('sculpture')) return 'https://images.unsplash.com/photo-1605721911519-3dfeb3be25e7?w=500&auto=format&fit=crop&q=60';
  if (n.includes('blue') || n.includes('paint')) return 'https://images.unsplash.com/photo-1541701494587-cb58502866ab?w=500&auto=format&fit=crop&q=60';
  return 'https://images.unsplash.com/photo-1579783900882-c0d3dad7b119?w=500&auto=format&fit=crop&q=60';
};

const formatDateTime = (dateStr) => {
  if (!dateStr) return '—';
  return new Date(dateStr).toLocaleString('vi-VN', {
    day: '2-digit', month: '2-digit', year: 'numeric',
    hour: '2-digit', minute: '2-digit'
  });
};

const formatPrice = (amount) => {
  if (!amount && amount !== 0) return '—';
  return parseFloat(amount).toLocaleString('vi-VN') + ' VNĐ';
};

export default function ProductCard({ session }) {
  const navigate = useNavigate();

  if (!session) return null;

  const isActive = session.status === 'ACTIVE';
  const isScheduled = session.status === 'SCHEDULED';
  const hasCurrentBid = session.currentHighestBid && parseFloat(session.currentHighestBid) > 0;

  const handleEnter = () => {
    if (isActive) {
      navigate(`/auction/${session.id}`);
    } else {
      navigate(`/product/${session.id}`);
    }
  };

  // Status badge config
  const badgeConfig = {
    ACTIVE:    { bg: 'success',   label: '🔴 Live Now' },
    SCHEDULED: { bg: 'warning',   label: '🗓 Upcoming' },
    ENDED:     { bg: 'secondary', label: 'Ended' },
    CANCELLED: { bg: 'danger',    label: 'Cancelled' },
  };
  const badge = badgeConfig[session.status] ?? { bg: 'secondary', label: session.status };

  return (
    <Card
      className="h-100 border-0 shadow-sm overflow-hidden product-card"
      onClick={handleEnter}
      style={{ cursor: 'pointer', transition: 'transform 0.2s, box-shadow 0.2s' }}
    >
      {/* Image */}
      <div className="position-relative" style={{ height: '220px', overflow: 'hidden' }}>
        <Card.Img
          variant="top"
          src={resolveImageUrl(session.itemImage) || getArtworkImage(session.itemName)}
          alt={session.itemName}
          className="h-100 w-100 object-fit-cover"
          style={{ transition: 'transform 0.3s ease' }}
        />
        {/* Status Badge */}
        <div className="position-absolute top-0 end-0 m-2">
          <Badge
            bg={badge.bg}
            className="text-uppercase shadow-sm px-2 py-1"
            style={{ fontSize: '0.72rem', letterSpacing: '0.5px' }}
          >
            {badge.label}
          </Badge>
        </div>
        {/* Active pulse overlay */}
        {isActive && (
          <div
            className="position-absolute bottom-0 start-0 w-100 d-flex align-items-center gap-2 px-3 py-2"
            style={{ background: 'linear-gradient(transparent, rgba(0,0,0,0.65))' }}
          >
            <Flame size={14} className="text-warning" />
            <span className="text-white small fw-semibold" style={{ fontSize: '0.78rem' }}>
              Đang diễn ra — Vào ngay!
            </span>
          </div>
        )}
      </div>

      <Card.Body className="d-flex flex-column p-3 bg-white">
        {/* Item Name */}
        <Card.Title
          className="fw-bold mb-1 text-dark"
          style={{ fontSize: '1rem', lineHeight: '1.3', display: '-webkit-box', WebkitLineClamp: 2, WebkitBoxOrient: 'vertical', overflow: 'hidden' }}
        >
          {session.itemName || 'Untitled Item'}
        </Card.Title>

        {/* Session ID tag */}
        <div className="text-muted mb-3" style={{ fontSize: '0.75rem' }}>
          Phiên #{session.id}
        </div>

        <div className="mt-auto">
          {/* Current Bid / Starting Price */}
          <div className="d-flex justify-content-between align-items-center mb-2">
            <span className="text-muted small text-uppercase" style={{ fontSize: '0.73rem', letterSpacing: '0.3px' }}>
              {hasCurrentBid ? 'Giá cao nhất:' : 'Giá khởi điểm:'}
            </span>
            <span className="fw-bold" style={{ color: '#004e64', fontSize: '1.05rem' }}>
              {hasCurrentBid
                ? formatPrice(session.currentHighestBid)
                : formatPrice(session.reservePrice)
              }
            </span>
          </div>

          {/* Time info */}
          <div className="d-flex align-items-center gap-2 mb-3 text-muted" style={{ fontSize: '0.76rem' }}>
            {isActive ? <Clock size={13} className="text-danger flex-shrink-0" /> : <CalendarClock size={13} className="flex-shrink-0" />}
            <span className="font-monospace" style={{ lineHeight: '1.3' }}>
              {isActive
                ? `Kết thúc: ${formatDateTime(session.endTime)}`
                : `Bắt đầu: ${formatDateTime(session.startTime)}`
              }
            </span>
          </div>

          {/* CTA Button */}
          <Button
            variant={isActive ? 'dark' : 'outline-dark'}
            className="w-100 py-2 text-uppercase fw-semibold d-flex align-items-center justify-content-center gap-2"
            style={{
              fontSize: '0.8rem',
              borderRadius: '4px',
              letterSpacing: '0.5px',
              backgroundColor: isActive ? '#004e64' : undefined,
              borderColor: isActive ? '#004e64' : undefined,
            }}
            onClick={(e) => {
              e.stopPropagation();
              handleEnter();
            }}
          >
            {isActive ? 'Vào phòng đấu giá' : 'Xem chi tiết'}
            <ArrowRight size={14} />
          </Button>
        </div>
      </Card.Body>
    </Card>
  );
}
