import { useContext, useEffect, useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { AppContext } from '../../../context/AppContext';
import { Container, Row, Col, Button, Form, Alert, Card, Table, Badge } from 'react-bootstrap';
import { ArrowLeft, Clock, Hammer, Eye, User, Calendar } from 'lucide-react';
import { productService } from '../services/productService';

export default function ProductDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { items, placeBid, incrementViewCount } = useContext(AppContext);

  const item = items.find(i => i.id === parseInt(id));

  // Bid input states
  const [bidAmount, setBidAmount] = useState('');
  const [errorMsg, setErrorMsg] = useState('');
  const [successMsg, setSuccessMsg] = useState('');

  // Increment view count on mount
  useEffect(() => {
    if (item) {
      incrementViewCount(item.id);
    }
  }, [id]);

  if (!item) {
    return (
      <Container className="py-5 text-center">
        <Alert variant="danger">
          <h3>Auction item not found</h3>
          <p>The product you are trying to view does not exist or has been removed.</p>
          <Button variant="outline-danger" as={Link} to="/">Return to Catalog</Button>
        </Alert>
      </Container>
    );
  }

  const handlePlaceBid = (e) => {
    e.preventDefault();
    setErrorMsg('');
    setSuccessMsg('');

    const amount = parseFloat(bidAmount);
    const isValid = productService.validateBid(item, amount);

    if (!isValid) {
      const minRequired = productService.getMinimumBidRequired(item);
      setErrorMsg(`Minimum bid allowed is $${minRequired} ($10 increment over current bid, or matching reserve).`);
      return;
    }

    // Place bid
    placeBid(item.id, amount, 'buyer_guest');
    setSuccessMsg(`Congratulations! Your bid of $${amount} was placed successfully.`);
    setBidAmount('');
  };

  // Helper date formatter
  const formatDate = (dateStr) => {
    if (!dateStr) return '';
    return new Date(dateStr).toLocaleString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    });
  };

  const isActive = item.status === 'Active';

  return (
    <Container className="py-5">
      {/* Back button */}
      <Button 
        variant="link" 
        onClick={() => navigate('/')} 
        className="text-decoration-none text-dark d-inline-flex align-items-center gap-1 mb-4 p-0 hover-teal"
      >
        <ArrowLeft size={16} />
        <span>Back to catalog</span>
      </Button>

      <Row className="g-5">
        {/* Left column: Product Image */}
        <Col lg={6}>
          <div className="border rounded bg-white overflow-hidden shadow-sm">
            <img 
              src={item.image} 
              alt={item.title} 
              className="w-100 object-fit-contain" 
              style={{ maxHeight: '500px', backgroundColor: '#fdfdfd' }}
            />
          </div>
        </Col>

        {/* Right column: Bidding Info */}
        <Col lg={6} className="text-start">
          <div className="d-flex align-items-center gap-2 mb-2">
            <Badge bg={isActive ? 'success' : 'warning'} className="text-uppercase px-3 py-2">
              {item.status}
            </Badge>
            <span className="text-muted d-flex align-items-center gap-1 small">
              <Eye size={16} /> {item.views} views
            </span>
          </div>

          <h1 className="fw-bold text-dark mb-1">{item.title}</h1>
          <p className="fs-5 text-muted mb-4 italic">by {item.artist}</p>

          <Card className="bg-light border-0 mb-4 shadow-xs">
            <Card.Body className="p-4">
              <div className="row text-center g-3">
                <div className="col-6 border-end">
                  <div className="text-muted small text-uppercase mb-1">Reserve Price</div>
                  <div className="fw-bold fs-4 text-dark">${item.reserve}</div>
                </div>
                <div className="col-6">
                  <div className="text-muted small text-uppercase mb-1">Current Bid</div>
                  <div className="fw-bold fs-4 text-teal" style={{ color: '#005f73' }}>
                    {item.currentBid ? `$${item.currentBid}` : 'No bids yet'}
                  </div>
                </div>
              </div>
            </Card.Body>
          </Card>

          <div className="mb-4">
            <h5 className="fw-bold text-dark">Description</h5>
            <p className="text-muted small" style={{ lineHeight: '1.6' }}>
              {item.description}
            </p>
          </div>

          <div className="mb-4">
            <div className="d-flex align-items-center gap-2 text-muted small mb-2">
              <Clock size={16} />
              <span>
                {isActive 
                  ? `Auction Ends: ${formatDate(item.endTime)}` 
                  : `Auction Starts: ${formatDate(item.startTime)}`
                }
              </span>
            </div>
          </div>

          {/* Place Bid Form */}
          {isActive ? (
            <div className="border p-4 rounded bg-white shadow-sm mb-4">
              <h5 className="fw-bold mb-3 d-flex align-items-center gap-2">
                <Hammer size={18} style={{ color: '#004e64' }} />
                <span>Place Your Bid</span>
              </h5>

              {errorMsg && <Alert variant="danger" className="py-2 small">{errorMsg}</Alert>}
              {successMsg && <Alert variant="success" className="py-2 small">{successMsg}</Alert>}

              <Form onSubmit={handlePlaceBid}>
                <Form.Group className="mb-3" controlId="bidInput">
                  <Form.Label className="small text-muted">
                    Bid Amount (USD) - Min bid: ${productService.getMinimumBidRequired(item)}
                  </Form.Label>
                  <Form.Control
                    type="number"
                    placeholder="Enter bid amount..."
                    value={bidAmount}
                    onChange={(e) => setBidAmount(e.target.value)}
                    required
                  />
                </Form.Group>
                <Button 
                  variant="dark" 
                  type="submit" 
                  className="w-100 py-2 text-uppercase fw-semibold"
                  style={{ backgroundColor: '#004e64', borderColor: '#004e64' }}
                >
                  Confirm Bid
                </Button>
              </Form>
            </div>
          ) : (
            <Alert variant="info" className="mb-4">
              <div className="d-flex align-items-center gap-2">
                <Calendar size={18} />
                <span>Bidding will open when the auction is Active.</span>
              </div>
            </Alert>
          )}

          {/* Bid History Table */}
          <div>
            <h5 className="fw-bold mb-3">Bid History</h5>
            {item.bids && item.bids.length > 0 ? (
              <Table responsive striped hover size="sm" className="small border align-middle">
                <thead className="bg-light">
                  <tr>
                    <th>Bidder</th>
                    <th>Bid Amount</th>
                    <th>Time</th>
                  </tr>
                </thead>
                <tbody>
                  {item.bids.map((bid, idx) => (
                    <tr key={idx}>
                      <td className="d-flex align-items-center gap-1 font-monospace py-2">
                        <User size={14} className="text-muted" />
                        {bid.bidder}
                      </td>
                      <td className="fw-bold text-dark">${bid.amount}</td>
                      <td className="text-muted small">{new Date(bid.time).toLocaleString()}</td>
                    </tr>
                  ))}
                </tbody>
              </Table>
            ) : (
              <p className="text-muted small italic">No bids have been placed yet for this item.</p>
            )}
          </div>
        </Col>
      </Row>
    </Container>
  );
}
