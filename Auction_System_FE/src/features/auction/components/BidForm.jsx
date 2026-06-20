import { useState, useContext } from 'react';
import { Form, Button, Alert } from 'react-bootstrap';
import { Hammer, LogIn } from 'lucide-react';
import { AuthContext } from '../../../context/AuthContext';

/**
 * BidForm Component.
 * Handlers bid amount inputs, validations, and placement commands.
 *
 * @param {Object} props
 * @param {number} props.currentPrice - Current highest bid amount or starting price
 * @param {function} props.onPlaceBid - Callback function when bid is valid and submitted
 * @param {boolean} props.isEnded - Whether the auction has ended
 */
export default function BidForm({ currentPrice, onPlaceBid, isEnded }) {
  const { isAuthenticated, openAuthModal } = useContext(AuthContext);
  const [bidValue, setBidValue] = useState('');
  const [errorMsg, setErrorMsg] = useState('');

  const minBidAllowed = currentPrice > 0 ? currentPrice + 1 : 1; // Assuming +1 increment fallback

  const handleSubmit = (e) => {
    e.preventDefault();
    setErrorMsg('');

    if (!isAuthenticated) {
      openAuthModal();
      return;
    }

    const value = parseFloat(bidValue);
    if (isNaN(value)) {
      setErrorMsg('Please enter a valid numeric amount.');
      return;
    }

    if (value <= 0) {
      setErrorMsg('Bid amount must be a positive value.');
      return;
    }

    if (value <= currentPrice) {
      setErrorMsg(`Your bid must be greater than the current price of $${currentPrice.toLocaleString()}.`);
      return;
    }

    onPlaceBid(value);
    setBidValue('');
  };

  const handleInputChange = (e) => {
    const val = e.target.value;
    
    // Prevent typing non-numeric negative values in input
    if (val !== '' && parseFloat(val) < 0) return;
    
    setBidValue(val);
    if (errorMsg) setErrorMsg('');
  };

  if (isEnded) {
    return (
      <Alert variant="secondary" className="text-center py-3 mb-0 shadow-xs">
        <p className="fw-semibold mb-0 text-muted">Bidding is closed as the auction has ended.</p>
      </Alert>
    );
  }

  if (!isAuthenticated) {
    return (
      <div className="border p-4 rounded bg-white shadow-sm text-center">
        <h5 className="fw-bold mb-3 d-flex align-items-center justify-content-center gap-2">
          <Hammer size={18} style={{ color: '#004e64' }} />
          <span>Place Your Bid</span>
        </h5>
        <p className="text-muted small mb-4">You must be logged in to bid on this live session.</p>
        <Button 
          variant="dark" 
          onClick={openAuthModal}
          className="w-100 py-2 d-flex align-items-center justify-content-center gap-2 text-uppercase fw-semibold"
          style={{ backgroundColor: '#004e64', borderColor: '#004e64' }}
        >
          <LogIn size={16} />
          <span>Sign In to Place Bid</span>
        </Button>
      </div>
    );
  }

  return (
    <div className="border p-4 rounded bg-white shadow-sm">
      <h5 className="fw-bold mb-3 d-flex align-items-center gap-2">
        <Hammer size={18} style={{ color: '#004e64' }} />
        <span>Place Your Bid</span>
      </h5>

      {errorMsg && (
        <Alert variant="danger" className="py-2 small mb-3">
          {errorMsg}
        </Alert>
      )}

      <Form onSubmit={handleSubmit}>
        <Form.Group className="mb-3" controlId="bidAmountInput">
          <Form.Label className="small text-muted">
            Bid Amount (USD) - Must be greater than ${currentPrice.toLocaleString()}
          </Form.Label>
          <Form.Control
            type="number"
            step="0.01"
            placeholder={`Enter $${minBidAllowed.toLocaleString()} or more...`}
            value={bidValue}
            onChange={handleInputChange}
            required
            disabled={isEnded}
            style={{ fontSize: '1.05rem', fontWeight: 500 }}
          />
        </Form.Group>
        
        <Button 
          variant="dark" 
          type="submit" 
          disabled={isEnded}
          className="w-100 py-2 text-uppercase fw-semibold"
          style={{ 
            backgroundColor: '#004e64', 
            borderColor: '#004e64', 
            transition: 'all 0.2s ease'
          }}
        >
          Confirm Bid
        </Button>
      </Form>
    </div>
  );
}
