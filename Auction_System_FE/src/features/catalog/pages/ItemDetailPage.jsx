import { useEffect, useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import {
  Container, Row, Col, Button, Alert, Card, Badge, Spinner
} from 'react-bootstrap';
import {
  ArrowLeft, Tag, DollarSign, Info, Package
} from 'lucide-react';
import { itemApi } from '../../../api/itemApi';
import { resolveImageUrl } from '../../../utils/imageUtils';

const getArtworkImage = (name = '') => {
  const n = name.toLowerCase();
  if (n.includes('mona') || n.includes('lisa')) return 'https://images.unsplash.com/photo-1580136579312-94651dfd596d?w=800&auto=format&fit=crop&q=80';
  if (n.includes('pot') || n.includes('ceramic')) return 'https://images.unsplash.com/photo-1612196808214-b8e1d6145a8c?w=800&auto=format&fit=crop&q=80';
  if (n.includes('bird') || n.includes('forest')) return 'https://images.unsplash.com/photo-1448375240586-882707db888b?w=800&auto=format&fit=crop&q=80';
  if (n.includes('apollo') || n.includes('sculpture')) return 'https://images.unsplash.com/photo-1605721911519-3dfeb3be25e7?w=800&auto=format&fit=crop&q=80';
  if (n.includes('blue') || n.includes('paint')) return 'https://images.unsplash.com/photo-1541701494587-cb58502866ab?w=800&auto=format&fit=crop&q=80';
  return 'https://images.unsplash.com/photo-1579783900882-c0d3dad7b119?w=800&auto=format&fit=crop&q=80';
};

const formatPrice = (amount) => {
  if (!amount && amount !== 0) return '\u2014';
  return parseFloat(amount).toLocaleString('en-US') + ' VND';
};

const statusConfig = {
  PENDING:   { bg: 'warning',   label: 'Pending Review', textClass: 'text-warning' },
  APPROVED:  { bg: 'success',   label: 'Approved',       textClass: 'text-success' },
  REJECTED:  { bg: 'danger',    label: 'Rejected',       textClass: 'text-danger' },
  SOLD:      { bg: 'info',      label: 'Sold',           textClass: 'text-info' },
};

export default function ItemDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();

  const [item, setItem] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (!id) return;
    setLoading(true);
    setError(null);

    itemApi.getItemById(id)
      .then((data) => setItem(data))
      .catch(() => setError('Unable to load the item. It may not exist or has been removed.'))
      .finally(() => setLoading(false));
  }, [id]);

  if (loading) {
    return (
      <Container className="py-5 text-center" style={{ minHeight: '60vh' }}>
        <Spinner animation="border" style={{ color: '#005f73', width: '3rem', height: '3rem' }} />
        <p className="text-muted mt-3">Loading item details...</p>
      </Container>
    );
  }

  if (error || !item) {
    return (
      <Container className="py-5">
        <Alert variant="danger" className="rounded-4">
          <h5 className="fw-bold">Item Not Found</h5>
          <p className="mb-3">{error || 'This item does not exist or has been removed.'}</p>
          <Button variant="outline-danger" as={Link} to="/user/items" size="sm">
            <ArrowLeft size={14} className="me-1" /> Back to My Items
          </Button>
        </Alert>
      </Container>
    );
  }

  const status = statusConfig[item.status] ?? { bg: 'secondary', label: item.status, textClass: 'text-secondary' };
  const itemImage = resolveImageUrl(item.imageUrl) || getArtworkImage(item.name);

  return (
    <Container className="py-5">
      <Button
        variant="link"
        onClick={() => navigate(-1)}
        className="text-decoration-none text-dark d-inline-flex align-items-center gap-1 mb-4 p-0"
      >
        <ArrowLeft size={16} />
        <span>Back</span>
      </Button>

      <Row className="g-5">
        <Col lg={6}>
          <div className="border rounded-4 bg-white overflow-hidden shadow-sm position-relative">
            <img
              src={itemImage}
              alt={item.name}
              className="w-100"
              style={{ maxHeight: '480px', objectFit: 'cover' }}
            />
            <div
              className="position-absolute bottom-0 start-0 w-100 d-flex align-items-center gap-2 px-4 py-2"
              style={{ background: 'linear-gradient(transparent, rgba(0,0,0,0.55))' }}
            >
              <Badge bg={status.bg} className="px-2 py-1 text-uppercase" style={{ fontSize: '0.78rem' }}>
                {status.label}
              </Badge>
              <span className="text-white small">Item #{item.id}</span>
            </div>
          </div>

          <div className="mt-4 d-flex gap-3">
            <Card className="flex-fill border-0 shadow-sm rounded-4 text-center p-3">
              <div className="text-muted small text-uppercase mb-1" style={{ fontSize: '0.72rem' }}>Starting Price</div>
              <div className="fw-bold" style={{ color: '#004e64', fontSize: '1.05rem' }}>
                {formatPrice(item.startingPrice)}
              </div>
            </Card>
            {item.reservePrice != null && (
              <Card className="flex-fill border-0 shadow-sm rounded-4 text-center p-3">
                <div className="text-muted small text-uppercase mb-1" style={{ fontSize: '0.72rem' }}>Reserve Price</div>
                <div className="fw-bold" style={{ color: '#004e64', fontSize: '1.05rem' }}>
                  {formatPrice(item.reservePrice)}
                </div>
              </Card>
            )}
          </div>
        </Col>

        <Col lg={6} className="text-start">
          <h1 className="fw-bold text-dark mb-1" style={{ fontSize: '1.9rem', lineHeight: '1.25' }}>
            {item.name}
          </h1>
          <p className="text-muted mb-4" style={{ fontSize: '0.9rem' }}>
            Item #{item.id}
          </p>

          {item.description && (
            <div className="mb-4">
              <h5 className="fw-bold text-dark mb-2 d-flex align-items-center gap-2">
                <Info size={16} /> Description
              </h5>
              <p className="text-muted" style={{ lineHeight: '1.7', fontSize: '0.92rem' }}>
                {item.description}
              </p>
            </div>
          )}

          <Card className="border-0 bg-light rounded-4 mb-4 p-3">
            <div className="d-flex flex-column gap-2">
              {item.categoryName && (
                <div className="d-flex align-items-center gap-3">
                  <Tag size={16} className="text-muted flex-shrink-0" />
                  <div>
                    <div className="text-muted small" style={{ fontSize: '0.75rem' }}>Category</div>
                    <div className="fw-semibold text-dark small">{item.categoryName}</div>
                  </div>
                </div>
              )}
              {item.sellerName && (
                <>
                  <hr className="my-1" />
                  <div className="d-flex align-items-center gap-3">
                    <Package size={16} className="text-muted flex-shrink-0" />
                    <div>
                      <div className="text-muted small" style={{ fontSize: '0.75rem' }}>Seller</div>
                      <div className="fw-semibold text-dark small">{item.sellerName}</div>
                    </div>
                  </div>
                </>
              )}
              <hr className="my-1" />
              <div className="d-flex align-items-center gap-3">
                <DollarSign size={16} className="text-muted flex-shrink-0" />
                <div>
                  <div className="text-muted small" style={{ fontSize: '0.75rem' }}>Price</div>
                  <div className="fw-semibold small" style={{ color: '#004e64' }}>{formatPrice(item.startingPrice)}</div>
                </div>
              </div>
            </div>
          </Card>

          {item.sessionId && (
            <div className="border rounded-4 p-4 bg-white shadow-sm mb-4">
              <h6 className="fw-bold mb-2">Auction Session</h6>
              <p className="text-muted small mb-3">This item has an associated auction session.</p>
              <Button
                variant="outline-dark"
                className="rounded-3"
                onClick={() => navigate(`/product/${item.sessionId}`)}
              >
                View Auction Session
              </Button>
            </div>
          )}

          <Button
            variant="outline-dark"
            onClick={() => navigate('/user/items')}
            className="d-flex align-items-center gap-2 rounded-3"
          >
            <ArrowLeft size={16} />
            Back to My Items
          </Button>
        </Col>
      </Row>
    </Container>
  );
}
