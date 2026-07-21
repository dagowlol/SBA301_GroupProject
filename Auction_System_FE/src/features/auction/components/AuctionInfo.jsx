import { Card, Badge } from 'react-bootstrap';
import { Tag, FileText, Info } from 'lucide-react';

/**
 * AuctionInfo Component.
 * Presents core item specifications, catalog tags, and session explanations.
 *
 * @param {Object} props
 * @param {Object} props.sessionDetail - Mapped frontend auction session model
 */
export default function AuctionInfo({ sessionDetail }) {
  if (!sessionDetail) return null;

  return (
    <Card className="border-0 shadow-sm mt-4 text-start">
      <Card.Header 
        className="px-4 py-3 border-bottom d-flex align-items-center gap-2"
        style={{ backgroundColor: '#fcfcfc' }}
      >
        <Info size={18} className="text-secondary" />
        <h5 className="fw-bold mb-0 text-dark">Item Details</h5>
      </Card.Header>
      
      <Card.Body className="p-4">
        {/* Category Tag */}
        <div className="d-flex align-items-center gap-2 mb-3">
          <Badge bg="info" className="bg-info-subtle text-info border d-flex align-items-center gap-1 px-3 py-2 text-uppercase">
            <Tag size={12} />
            <span>Collectible Art</span>
          </Badge>
          <span className="text-muted small">Item Ref ID: #{sessionDetail.itemId}</span>
        </div>

        {/* Name and Description */}
        <h4 className="fw-bold text-dark mb-3">{sessionDetail.itemName}</h4>
        
        <div className="mb-4">
          <h6 className="fw-bold text-secondary d-flex align-items-center gap-1.5 mb-2" style={{ fontSize: '0.9rem' }}>
            <FileText size={14} />
            <span>Description</span>
          </h6>
          <p 
            className="text-muted" 
            style={{ 
              lineHeight: '1.7', 
              fontSize: '0.95rem',
              whiteSpace: 'pre-line' 
            }}
          >
            {sessionDetail.description || 'No description provided for this catalog item.'}
          </p>
        </div>

        {/* Condition Check / Provenance Mock Details */}
        <div className="p-3 bg-light rounded border border-light-subtle small text-muted">
          <div className="fw-semibold text-dark mb-1">Authenticity & Provenance</div>
          This item has been reviewed and verified by the Annexe Auction Staff panel. Bidding is legally binding and subject to the Terms & Conditions of the catalog platform.
        </div>
      </Card.Body>
    </Card>
  );
}
