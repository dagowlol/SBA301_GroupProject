import { useEffect, useState, useContext, useRef } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { Container, Row, Col, Spinner, Alert, Card, Toast, ToastContainer, Button, Badge } from 'react-bootstrap';
import { ArrowLeft, Crown, Sparkles, User, HelpCircle } from 'lucide-react';

import { AuthContext } from '../../../context/AuthContext';
import { auctionService } from '../services/auctionService';
import { auctionSocketService } from '../services/auctionSocketService';
import { auctionMapper } from '../mappers/auctionMapper';

import CountdownTimer from '../components/CountdownTimer';
import BidForm from '../components/BidForm';
import BidHistoryTable from '../components/BidHistoryTable';
import AuctionInfo from '../components/AuctionInfo';

export default function AuctionRoom() {
  const { sessionId } = useParams();
  const navigate = useNavigate();
  const { user } = useContext(AuthContext);

  const [sessionDetail, setSessionDetail] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // UI state
  const [activeImage, setActiveImage] = useState('');
  const [thumbnails, setThumbnails] = useState([]);
  const [toasts, setToasts] = useState([]);
  const [priceFlash, setPriceFlash] = useState(false);
  const [isTimeUp, setIsTimeUp] = useState(false);

  // Keep ref to avoid duplicate subscriptions
  const isSubscribedRef = useRef(false);

  const addToast = (message) => {
    setToasts((prev) => [...prev, { id: Date.now(), message }]);
  };

  // 1. Fetch REST details on mount or sessionId change
  useEffect(() => {
    let active = true;
    setLoading(true);
    setError(null);
    setSessionDetail(null);
    isSubscribedRef.current = false;

    async function fetchSession() {
      try {
        const detail = await auctionService.getAuctionSessionDetail(sessionId);
        if (active) {
          setSessionDetail(detail);
          setActiveImage(detail.itemImage);
          // Create 3 thumbnails from the item image
          setThumbnails([
            detail.itemImage,
            detail.itemImage + '?auto=format&fit=crop&w=500&q=60&blur=20', // alternate 1 (simulated)
            detail.itemImage + '?auto=format&fit=crop&w=500&q=60&sat=-50',  // alternate 2 (simulated)
          ]);
          setLoading(false);
        }
      } catch (err) {
        if (active) {
          setError(err.message || 'Failed to load live auction room.');
          setLoading(false);
        }
      }
    }

    fetchSession();

    return () => {
      active = false;
    };
  }, [sessionId]);

  // 2. Connect and subscribe WebSocket when sessionDetail is loaded
  useEffect(() => {
    if (!sessionDetail || isSubscribedRef.current) return;

    isSubscribedRef.current = true;

    // Connect immediately
    auctionSocketService.connect(() => {
      // Once connected, subscribe to channels
      const unsubscribe = auctionSocketService.subscribeAuction(
        sessionId,
        (broadcast) => {
          // Receive new bid
          setPriceFlash(true);
          setTimeout(() => setPriceFlash(false), 800);

          setSessionDetail((prev) => {
            if (!prev) return null;
            const newBid = auctionMapper.broadcastToBidLog(broadcast);
            return {
              ...prev,
              currentPrice: broadcast.currentPrice,
              currentWinnerName: broadcast.winnerName,
              endTime: broadcast.endTime,
              recentBids: [newBid, ...prev.recentBids]
            };
          });
        },
        (socketError) => {
          // Receive private error message
          addToast(socketError.message || 'An error occurred while placing your bid.');
        }
      );

      // Save unsubscribe handler on window for cleanup
      window.currentSocketUnsubscribe = unsubscribe;
    });

    return () => {
      if (window.currentSocketUnsubscribe) {
        window.currentSocketUnsubscribe();
        window.currentSocketUnsubscribe = null;
      }
      auctionSocketService.disconnect();
      isSubscribedRef.current = false;
    };
  }, [sessionId, sessionDetail]);

  const handlePlaceBid = (amount) => {
    if (!user || !user.id) {
      addToast('Please login to place a bid.');
      return;
    }
    const success = auctionSocketService.sendBid(sessionId, user.id, amount);
    if (!success) {
      addToast('WebSocket connection is not active. Please wait or reload.');
    }
  };

  const handleTimeUp = () => {
    setIsTimeUp(true);
  };

  if (loading) {
    return (
      <Container className="d-flex align-items-center justify-content-center" style={{ minHeight: '60vh' }}>
        <div className="text-center">
          <Spinner animation="border" variant="primary" style={{ width: '3rem', height: '3rem' }} />
          <p className="mt-3 text-muted">Entering Live Auction Room...</p>
        </div>
      </Container>
    );
  }

  if (error) {
    return (
      <Container className="py-5">
        <Alert variant="danger" className="text-center p-5 shadow-xs rounded border-danger-subtle">
          <HelpCircle size={48} className="text-danger mb-3" />
          <h3 className="fw-bold">Auction Room Not Found</h3>
          <p className="text-muted mb-4">{error}</p>
          <Button variant="outline-danger" className="px-4 py-2" onClick={() => navigate('/auction')}>
            Back to Catalog
          </Button>
        </Alert>
      </Container>
    );
  }

  if (!sessionDetail) return null;

  return (
    <Container className="py-5">
      {/* Toast Notifications */}
      <ToastContainer position="top-end" className="p-3" style={{ position: 'fixed', zIndex: 9999 }}>
        {toasts.map((t) => (
          <Toast 
            key={t.id} 
            bg="danger" 
            onClose={() => setToasts((prev) => prev.filter((toast) => toast.id !== t.id))}
            delay={5000}
            autohide
            className="border-0 shadow-lg text-white"
          >
            <Toast.Header className="bg-danger text-white border-0 py-2">
              <strong className="me-auto">Bidding Restriction</strong>
            </Toast.Header>
            <Toast.Body className="py-3" style={{ backgroundColor: '#dc2626' }}>
              {t.message}
            </Toast.Body>
          </Toast>
        ))}
      </ToastContainer>

      {/* Back to Catalog Nav */}
      <Button 
        variant="link" 
        onClick={() => navigate('/auction')} 
        className="text-decoration-none text-dark d-inline-flex align-items-center gap-1 mb-4 p-0 hover-teal"
      >
        <ArrowLeft size={16} />
        <span>Return to Auction List</span>
      </Button>

      {/* Main Grid: Left Column (Images), Right Column (Timer, Price, Bidding) */}
      <Row className="g-5">
        {/* Left Column: Image Gallery */}
        <Col lg={6}>
          <div className="border rounded bg-white overflow-hidden shadow-sm d-flex justify-content-center align-items-center mb-3" style={{ height: '450px' }}>
            <img 
              src={activeImage} 
              alt={sessionDetail.itemName} 
              className="w-100 h-100 object-fit-contain" 
              style={{ backgroundColor: '#fafafa', transition: 'all 0.4s ease' }}
            />
          </div>
          
          {/* Thumbnail row */}
          <div className="d-flex gap-3 justify-content-start overflow-x-auto pb-2 scrollbar-hidden">
            {thumbnails.map((thumb, idx) => (
              <div 
                key={idx}
                onClick={() => setActiveImage(thumb)}
                className="border rounded overflow-hidden cursor-pointer"
                style={{ 
                  width: '80px', 
                  height: '80px', 
                  borderColor: activeImage === thumb ? '#0077b6' : '#dee2e6',
                  borderWidth: activeImage === thumb ? '3px' : '1px',
                  opacity: activeImage === thumb ? 1 : 0.6,
                  transition: 'all 0.2s ease',
                  flexShrink: 0
                }}
              >
                <img src={thumb} alt={`View ${idx + 1}`} className="w-100 h-100 object-fit-cover" />
              </div>
            ))}
          </div>

          {/* Details below description */}
          <AuctionInfo sessionDetail={sessionDetail} />
        </Col>

        {/* Right Column: Pricing & Real-time actions */}
        <Col lg={6} className="text-start">
          <div className="d-flex align-items-center gap-2 mb-2">
            <Badge bg={isTimeUp ? 'danger' : 'success'} className="text-uppercase px-3 py-2">
              {isTimeUp ? 'Ended' : 'Live'}
            </Badge>
            <span className="text-muted small font-monospace d-flex align-items-center gap-1">
              <Sparkles size={14} className="text-warning animate-bounce" /> Real-time active
            </span>
          </div>

          <h2 className="fw-bold text-dark mb-1">{sessionDetail.itemName}</h2>
          <p className="text-muted italic mb-4">Live Session #{sessionDetail.sessionId}</p>

          {/* Pricing Box with Flash micro-animation */}
          <Card className="border-0 mb-4 shadow-sm" style={{ transition: 'all 0.3s ease' }}>
            <Card.Body 
              className="p-4 rounded" 
              style={{ 
                backgroundColor: priceFlash ? '#ecfdf5' : '#f8f9fa', 
                borderLeft: priceFlash ? '5px solid #10b981' : '5px solid #003d5b',
                transition: 'background-color 0.4s ease' 
              }}
            >
              <div className="row g-3 align-items-center">
                <div className="col-md-6 text-center text-md-start">
                  <div className="text-muted small text-uppercase mb-1 fw-bold tracking-wider">Current Price</div>
                  <div 
                    className="fw-bold display-6 font-monospace"
                    style={{ 
                      color: priceFlash ? '#0f9f83' : '#003d5b',
                      transition: 'color 0.3s ease'
                    }}
                  >
                    ${sessionDetail.currentPrice ? sessionDetail.currentPrice.toLocaleString('en-US', { minimumFractionDigits: 2 }) : '0.00'}
                  </div>
                </div>
                <div className="col-md-6 border-start-md text-center text-md-start">
                  <div className="text-muted small text-uppercase mb-1 fw-bold tracking-wider">Current Winner</div>
                  <div className="fw-semibold fs-5 text-dark d-flex align-items-center justify-content-center justify-content-md-start gap-1.5 mt-1">
                    {sessionDetail.currentWinnerName ? (
                      <>
                        <Crown size={18} className="text-warning fill-warning" />
                        <span>{sessionDetail.currentWinnerName}</span>
                      </>
                    ) : (
                      <>
                        <User size={18} className="text-muted" />
                        <span className="text-muted italic">No bids yet</span>
                      </>
                    )}
                  </div>
                </div>
              </div>
            </Card.Body>
          </Card>

          {/* Timer Card */}
          <div className="mb-4">
            <CountdownTimer endTime={sessionDetail.endTime} onTimeUp={handleTimeUp} />
          </div>

          {/* Place Bid Form */}
          <div className="mb-4">
            <BidForm 
              currentPrice={sessionDetail.currentPrice} 
              onPlaceBid={handlePlaceBid} 
              isEnded={isTimeUp} 
            />
          </div>

          {/* Bid History table */}
          <div>
            <BidHistoryTable bids={sessionDetail.recentBids} />
          </div>
        </Col>
      </Row>
    </Container>
  );
}
