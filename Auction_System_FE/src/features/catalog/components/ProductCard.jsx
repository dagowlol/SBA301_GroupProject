import { Card, Button, Badge } from 'react-bootstrap';
import { useNavigate } from 'react-router-dom';
import { Clock, Tag } from 'lucide-react';

export default function ProductCard({ item }) {
  const navigate = useNavigate();

  // Handle navigate to detail
  const handleCardClick = () => {
    navigate(`/product/${item.id}`);
  };

  // Format date helper
  const formatDate = (dateStr) => {
    if (!dateStr) return '';
    const d = new Date(dateStr);
    return `${d.getDate()}.${d.getMonth() + 1}.${d.getFullYear()} ${d.getHours().toString().padStart(2, '0')}:${d.getMinutes().toString().padStart(2, '0')}:00 GMT+8`;
  };

  // Active status color helper
  const isActive = item.status === 'Active';
  
  return (
    <Card 
      className="h-100 border-0 shadow-sm overflow-hidden product-card"
      onClick={handleCardClick}
      style={{ cursor: 'pointer', transition: 'transform 0.2s, box-shadow 0.2s' }}
    >
      <div className="position-relative" style={{ height: '260px', overflow: 'hidden' }}>
        <Card.Img 
          variant="top" 
          src={item.image} 
          alt={item.title}
          className="h-100 w-100 object-fit-cover transition-transform"
          style={{ transition: 'transform 0.3s ease' }}
        />
        <div className="position-absolute top-2 right-2">
          <Badge bg={isActive ? "success" : "warning"} className="text-uppercase shadow-sm">
            {item.status}
          </Badge>
        </div>
      </div>
      
      <Card.Body className="d-flex flex-column text-start p-3 bg-white">
        <Card.Title className="fw-bold mb-0 text-truncate text-dark" style={{ fontSize: '1.15rem' }}>
          {item.title}
        </Card.Title>
        <div className="text-muted small mb-3 italic">
          by {item.artist}
        </div>
        
        <div className="mt-auto">
          <div className="d-flex justify-content-between align-items-center mb-2">
            <span className="text-muted small text-uppercase">Current Bid:</span>
            <span className="fw-bold text-dark fs-5">
              {item.currentBid ? `$${item.currentBid}` : '-'}
            </span>
          </div>

          <div className="d-flex align-items-start gap-2 mb-3" style={{ minHeight: '34px' }}>
            <span 
              className="rounded-circle d-inline-block mt-1 flex-shrink-0"
              style={{ 
                width: '8px', 
                height: '8px', 
                backgroundColor: isActive ? '#2ec4b6' : '#ff9f1c' 
              }}
            />
            <span className="text-muted font-monospace" style={{ fontSize: '0.75rem', lineHeight: '1.2' }}>
              {isActive 
                ? `auction ends in: ${formatDate(item.endTime)}` 
                : `auction starts in: ${formatDate(item.startTime)}`
              }
            </span>
          </div>

          <Button 
            variant="outline-dark" 
            className="w-100 py-2 text-uppercase fw-semibold btn-bid-now"
            style={{ fontSize: '0.8rem', borderRadius: '4px', letterSpacing: '0.5px' }}
            onClick={(e) => {
              e.stopPropagation();
              handleCardClick();
            }}
          >
            Bid now
          </Button>
        </div>
      </Card.Body>
    </Card>
  );
}
