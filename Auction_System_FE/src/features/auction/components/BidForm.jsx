import { useState, useContext } from 'react';
import { Form, Button, Alert, InputGroup, Spinner } from 'react-bootstrap';
import { Hammer, LogIn, Cpu, XCircle } from 'lucide-react';
import { AuthContext } from '../../../context/AuthContext';

/**
 * BidForm Component.
 * Handlers bid amount inputs, validations, and placement commands.
 */
export default function BidForm({ 
  currentPrice, 
  minimumIncrement, 
  onPlaceBid, 
  isEnded,
  autoBidConfig,
  onConfigureAutoBid,
  onDisableAutoBid
}) {
  const { isAuthenticated, openAuthModal } = useContext(AuthContext);
  const [bidValue, setBidValue] = useState('');
  const [errorMsg, setErrorMsg] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const minBidAllowed = currentPrice 
    ? currentPrice + minimumIncrement 
    : minimumIncrement;

  const handleSubmit = async (e) => {
    e.preventDefault();
    setErrorMsg('');

    if (!isAuthenticated) {
      openAuthModal();
      return;
    }

    const value = parseFloat(bidValue);
    if (isNaN(value)) {
      setErrorMsg('Please enter a valid bid amount.');
      return;
    }

    if (value < minBidAllowed) {
      setErrorMsg(`Your bid must be at least ${minBidAllowed.toLocaleString()} VND.`);
      return;
    }

    setSubmitting(true);
    try {
      const success = onPlaceBid(value);
      if (success) {
        setBidValue('');
      } else {
        setErrorMsg('The live connection was lost. Please reload the page.');
      }
    } catch (err) {
      setErrorMsg(err.message || 'Failed to place the bid.');
    } finally {
      setSubmitting(false);
    }
  };

  if (isEnded) {
    return (
      <Alert variant="secondary" className="text-center py-3 mb-0 shadow-sm rounded-4 border-0">
        <p className="fw-semibold mb-0 text-muted">This auction has ended. Bidding is closed.</p>
      </Alert>
    );
  }

  if (!isAuthenticated) {
    return (
      <div className="border p-4 rounded-4 bg-white shadow-sm text-center">
        <h5 className="fw-bold mb-3 d-flex align-items-center justify-content-center gap-2" style={{ color: '#004e64' }}>
          <Hammer size={18} style={{ color: '#004e64' }} />
          <span>Join the Auction</span>
        </h5>
        <p className="text-muted small mb-4">Please sign in to participate in live bidding.</p>
        <Button 
          variant="dark" 
          onClick={openAuthModal}
          className="w-100 py-2 d-flex align-items-center justify-content-center gap-2 text-uppercase fw-semibold"
          style={{ backgroundColor: '#004e64', borderColor: '#004e64' }}
        >
          <LogIn size={16} />
          <span>Sign in to Bid</span>
        </Button>
      </div>
    );
  }

  return (
    <div className="border p-4 rounded-4 bg-white shadow-sm">
      <div className="d-flex justify-content-between align-items-center mb-3">
        <h5 className="fw-bold mb-0 d-flex align-items-center gap-2" style={{ color: '#004e64' }}>
          <Hammer size={18} style={{ color: '#004e64' }} />
          <span>Join the Auction</span>
        </h5>
        <Button 
          variant="outline-primary" 
          size="sm" 
          className="d-flex align-items-center gap-1 px-3 py-1 rounded-pill"
          onClick={onConfigureAutoBid}
          style={{ fontSize: '0.8rem', fontWeight: 600 }}
        >
          <Cpu size={14} />
          <span>Auto-Bid</span>
        </Button>
      </div>

      {/* Robot Status (AC3) */}
      {autoBidConfig && autoBidConfig.isActive && (
        <div className="alert alert-info py-2 px-3 rounded-4 d-flex justify-content-between align-items-center mb-3 border-0 small">
          <div className="d-flex align-items-center gap-2 text-info-emphasis">
            <Cpu size={16} className="text-info animate-pulse" />
            <span className="fw-medium">
              Auto-bid enabled — Limit: {autoBidConfig.maxBidAmount.toLocaleString('en-US')} VND
            </span>
          </div>
          <Button 
            variant="link" 
            className="text-danger p-0 d-flex align-items-center gap-1 text-decoration-none fw-bold shadow-none"
            onClick={onDisableAutoBid}
            style={{ fontSize: '0.8rem' }}
          >
            <XCircle size={14} />
            <span>Disable Auto-bid</span>
          </Button>
        </div>
      )}

      {errorMsg && (
        <Alert variant="danger" className="py-2 small mb-3 border-0">
          {errorMsg}
        </Alert>
      )}

      <Form onSubmit={handleSubmit}>
        <Form.Group className="mb-3" controlId="bidAmountInput">
          <InputGroup size="lg" className="shadow-xs rounded-3 overflow-hidden">
            <InputGroup.Text className="bg-white text-muted fw-bold border-end-0 ps-3">VND</InputGroup.Text>
            <Form.Control
              type="number"
              step="0.01"
              placeholder={`Enter ${minBidAllowed.toLocaleString()} or more...`}
              value={bidValue}
              onChange={(e) => setBidValue(e.target.value)}
              required
              disabled={isEnded || submitting}
              style={{ fontSize: '1rem', fontWeight: 500 }}
            />
          </InputGroup>
          <Form.Text className="text-muted small mt-2 ms-1 d-block">
            Minimum bid increment: <strong>{minimumIncrement.toLocaleString()} VND</strong>
          </Form.Text>
        </Form.Group>
        
        <Button 
          variant="dark" 
          type="submit" 
          disabled={isEnded || submitting}
          className="w-100 py-3 text-uppercase fw-bold rounded-3 shadow-xs"
          style={{ 
            backgroundColor: '#004e64', 
            borderColor: '#004e64', 
            fontSize: '0.9rem',
            transition: 'all 0.2s ease'
          }}
        >
          {submitting ? <Spinner size="sm" animation="border" /> : 'Confirm Bid'}
        </Button>
      </Form>
    </div>
  );
}
