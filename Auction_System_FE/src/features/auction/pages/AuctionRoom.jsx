import { useEffect, useState, useContext } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useQueryClient } from '@tanstack/react-query';
import { Container, Row, Col, Spinner, Alert, Badge, Button, ListGroup, Card } from 'react-bootstrap';
import { ArrowLeft, Crown, Clock, HelpCircle, AlertCircle } from 'lucide-react';
import { message } from 'antd';

import { AuthContext } from '../../../context/AuthContext';
import { useAuctionSessionDetail } from '../hooks/useAuctionSessionDetail';
import { useBidWebSocket } from '../hooks/useBidWebSocket';
import CountdownTimer from '../components/CountdownTimer';
import BidForm from '../components/BidForm';
import AutoBidModal from '../components/AutoBidModal';
import { auctionApi } from '../../../api/auctionApi';
import { paymentApi } from '../../../api/paymentApi';

export default function AuctionRoom() {
  const { sessionId } = useParams();
  const navigate = useNavigate();
  const { user } = useContext(AuthContext);
  const queryClient = useQueryClient();

  // Hooks
  const [autoBidConfig, setAutoBidConfig] = useState(null);
  const [autoBidModalOpen, setAutoBidModalOpen] = useState(false);
  const [payment, setPayment] = useState(null);
  const [loadingPayment, setLoadingPayment] = useState(false);

  const { data: sessionDetail, isLoading, error } = useAuctionSessionDetail(sessionId);
  const { latestBid, wsError, placeBid, clearWsError } = useBidWebSocket(sessionId, (limitPayload) => {
    message.warning('Robot đã tắt - Tài khoản của bạn đã chạm hạn mức tối đa của Auto-Bid.');
    setAutoBidConfig(prev => prev ? { ...prev, isActive: false } : null);
  });

  const [isTimeUp, setIsTimeUp] = useState(false);

  // Sync cache with latestBid
  useEffect(() => {
    if (latestBid) {
      queryClient.setQueryData(['sessionDetail', sessionId], (oldData) => {
        if (!oldData) return oldData;
        return {
          ...oldData,
          currentPrice: latestBid.amount,
          currentWinnerName: latestBid.bidderName,
          recentBids: [
            latestBid,
            ...(oldData.recentBids || []).slice(0, 9)
          ]
        };
      });
    }
  }, [latestBid, sessionId, queryClient]);

  // Load Auto-Bid configuration
  useEffect(() => {
    if (sessionId && user?.id) {
      auctionApi.getAutoBidConfig(sessionId)
        .then(config => {
          if (config && config.id) {
            setAutoBidConfig(config);
          }
        })
        .catch(err => console.error("Error loading auto-bid config:", err));
    }
  }, [sessionId, user]);

  useEffect(() => {
    let cancelled = false;

    const fetchPaymentWithRetry = async (retries = 5, delayMs = 1500) => {
      for (let attempt = 1; attempt <= retries; attempt++) {
        if (cancelled) return;
        try {
          const data = await paymentApi.getMyPayment(sessionId);
          if (!cancelled) {
            setPayment(data);
            setLoadingPayment(false);
          }
          return; // success
        } catch (err) {
          console.warn(`[Payment] Attempt ${attempt}/${retries} failed:`, err?.message);
          if (attempt < retries) {
            await new Promise(resolve => setTimeout(resolve, delayMs * attempt));
          } else {
            console.error('[Payment] All retry attempts exhausted. Payment not found.');
            if (!cancelled) setLoadingPayment(false);
          }
        }
      }
    };

    if (sessionDetail?.status === 'ENDED' && sessionDetail?.winnerId === user?.id) {
      setLoadingPayment(true);
      fetchPaymentWithRetry();
    }

    return () => { cancelled = true; };
  }, [sessionDetail?.status, sessionDetail?.winnerId, user?.id, sessionId]);

  const handlePayWithVNPay = async () => {
    if (!payment?.id) return;
    setLoadingPayment(true);
    try {
      const res = await paymentApi.createVNPayUrl(payment.id);
      if (res && res.url) {
        window.location.href = res.url;
      } else {
        message.error('Không nhận được đường dẫn thanh toán.');
      }
    } catch (err) {
      message.error('Có lỗi xảy ra: ' + err.message);
    } finally {
      setLoadingPayment(false);
    }
  };

  const handleDisableAutoBid = async () => {
    if (!autoBidConfig) return;
    try {
      const updated = await auctionApi.saveAutoBidConfig(sessionId, {
        maxBidAmount: autoBidConfig.maxBidAmount,
        bidIncrement: autoBidConfig.bidIncrement,
        isActive: false
      });
      setAutoBidConfig(updated);
      message.success('Đã hủy chế độ đặt giá tự động.');
    } catch (err) {
      message.error('Không thể hủy chế độ tự động: ' + err.message);
    }
  };

  const handleConfigSaved = (newConfig) => {
    setAutoBidConfig(newConfig);
  };

  // Derived states
  const isEnded = isTimeUp || sessionDetail?.status !== 'ACTIVE';

  const handleTimeUp = () => setIsTimeUp(true);

  if (isLoading) {
    return (
      <Container className="d-flex flex-column align-items-center justify-content-center" style={{ minHeight: '60vh' }}>
        <Spinner animation="border" style={{ color: '#004e64', width: '3rem', height: '3rem' }} />
        <div className="mt-3 text-muted">Đang kết nối vào phòng...</div>
      </Container>
    );
  }

  if (error || !sessionDetail) {
    return (
      <Container className="py-5">
        <Alert variant="danger" className="text-center p-5 shadow-sm rounded border-0 bg-white">
          <HelpCircle size={48} className="text-danger mb-3" />
          <h3 className="fw-bold">Phòng Đấu Giá Không Tồn Tại</h3>
          <p className="text-muted mb-4">{error?.message || 'Không thể lấy thông tin phiên đấu giá.'}</p>
          <Button variant="outline-danger" className="px-4 py-2" onClick={() => navigate('/auction')}>
            Quay lại danh mục
          </Button>
        </Alert>
      </Container>
    );
  }

  return (
    <Container className="py-5">
      <Button
        variant="link"
        onClick={() => navigate('/auction')}
        className="text-decoration-none text-dark d-inline-flex align-items-center gap-2 mb-4 p-0 fw-medium"
      >
        <ArrowLeft size={18} /> Quay lại danh mục
      </Button>

      {wsError && (
        <Alert variant="danger" onClose={clearWsError} dismissible className="d-flex align-items-center gap-2 border-0 shadow-sm">
          <AlertCircle size={20} />
          <span><strong>Lỗi Đặt Giá:</strong> {wsError}</span>
        </Alert>
      )}

      <Row className="g-5">
        {/* LEFT COLUMN: Image, Title, Description, Countdown */}
        <Col lg={7}>
          <div className="border rounded-4 bg-white overflow-hidden shadow-sm d-flex justify-content-center align-items-center mb-4" style={{ height: '450px' }}>
            <img
              src={sessionDetail.itemImage}
              alt={sessionDetail.itemName}
              className="w-100 h-100 object-fit-contain p-3"
            />
          </div>

          <div className="mb-4">
            <h2 className="fw-bold text-dark mb-2">{sessionDetail.itemName}</h2>
            <p className="text-muted" style={{ lineHeight: '1.6' }}>{sessionDetail.description}</p>
          </div>

          <Card className="border-0 shadow-sm bg-light rounded-4 overflow-hidden">
            <Card.Body className="p-4 text-center">
              {isEnded ? (
                <Badge bg="danger" className="fs-5 px-4 py-2 rounded-pill shadow-sm tracking-wider">ĐÃ KẾT THÚC</Badge>
              ) : (
                <>
                  <div className="text-muted small text-uppercase mb-2 fw-bold d-flex align-items-center justify-content-center gap-1 tracking-wider">
                    <Clock size={16} /> Thời gian còn lại
                  </div>
                  <CountdownTimer endTime={sessionDetail.endTime} onTimeUp={handleTimeUp} />
                </>
              )}
            </Card.Body>
          </Card>
        </Col>

        {/* RIGHT COLUMN: Current Price, Bid Form, Bid Logs */}
        <Col lg={5}>
          {/* Current Price */}
          <Card className="border-0 shadow-sm rounded-4 mb-4 overflow-hidden" style={{ backgroundColor: '#f8f9fa' }}>
            <div style={{ height: '6px', backgroundColor: '#004e64', width: '100%' }}></div>
            <Card.Body className="p-4 p-xl-5">
              <div className="text-muted small text-uppercase fw-bold tracking-wider mb-2">Giá hiện tại</div>
              <div className="fw-bold display-4 font-monospace mb-3" style={{ color: '#004e64', letterSpacing: '-1px' }}>
                ${sessionDetail.currentPrice.toLocaleString('en-US', { minimumFractionDigits: 2 })}
              </div>
              <div className="d-flex align-items-center gap-2 fw-medium px-3 py-2 rounded" style={{ backgroundColor: '#e9ecef' }}>
                <Crown size={20} className="text-warning" style={{ fill: 'currentColor' }} />
                <span className="text-dark">
                  {sessionDetail.currentWinnerName || <span className="text-muted fst-italic">Chưa có người đặt giá</span>}
                </span>
              </div>
            </Card.Body>
          </Card>

          {/* Winner Payment Card */}
          {sessionDetail.status === 'ENDED' && sessionDetail.winnerId === user?.id && (
            <Card className="border-0 shadow-sm rounded-4 mb-4 overflow-hidden" style={{ backgroundColor: '#fffbeb', border: '1px solid #fef3c7' }}>
              <div style={{ height: '6px', backgroundColor: '#d97706', width: '100%' }}></div>
              <Card.Body className="p-4">
                <h5 className="fw-bold text-warning-emphasis mb-2">Chúc mừng! Bạn đã thắng phiên đấu giá</h5>
                <p className="text-muted small mb-3">
                  Giá thắng: <strong className="text-dark">${sessionDetail.currentPrice.toLocaleString('en-US', { minimumFractionDigits: 2 })}</strong>.
                  Vui lòng hoàn tất thanh toán của bạn qua VNPay bên dưới.
                </p>
                {payment ? (
                  payment.status === 'PENDING' ? (
                    <Button
                      variant="warning"
                      className="w-100 py-2.5 fw-semibold text-white d-flex align-items-center justify-content-center gap-2"
                      style={{ backgroundColor: '#d97706', borderColor: '#d97706' }}
                      onClick={handlePayWithVNPay}
                      disabled={loadingPayment}
                    >
                      Thanh toán bằng VNPay
                    </Button>
                  ) : (
                    <Badge bg="success" className="w-100 py-2.5 fs-6 rounded-3">
                      ĐÃ THANH TOÁN ({payment.status})
                    </Badge>
                  )
                ) : (
                  <Button variant="secondary" className="w-100 py-2.5" disabled>
                    Đang chuẩn bị thông tin thanh toán...
                  </Button>
                )}
              </Card.Body>
            </Card>
          )}

          {/* Bid Form */}
          <div className="mb-4">
            <BidForm
              currentPrice={sessionDetail.currentPrice}
              minimumIncrement={sessionDetail.minimumIncrement}
              onPlaceBid={(amount) => placeBid(user?.id, amount)}
              isEnded={isEnded}
              autoBidConfig={autoBidConfig}
              onConfigureAutoBid={() => setAutoBidModalOpen(true)}
              onDisableAutoBid={handleDisableAutoBid}
            />
          </div>

          {/* Bid Logs */}
          <div>
            <h6 className="fw-bold text-muted text-uppercase tracking-wider mb-3 ms-1" style={{ fontSize: '0.8rem' }}>Lịch sử đặt giá (Top 10)</h6>
            <ListGroup variant="flush" className="border rounded-4 shadow-sm bg-white overflow-hidden">
              {sessionDetail.recentBids && sessionDetail.recentBids.length > 0 ? (
                sessionDetail.recentBids.map((bid, idx) => (
                  <ListGroup.Item
                    key={bid.bidId || idx}
                    className="d-flex justify-content-between align-items-center py-3 px-4 border-bottom"
                    style={{ backgroundColor: idx === 0 ? '#f0fdf4' : 'white', transition: 'background-color 0.5s ease' }}
                  >
                    <div>
                      <div className="fw-semibold text-dark d-flex align-items-center gap-2">
                        {bid.bidderName}
                        {idx === 0 && <Badge bg="success" className="rounded-pill" style={{ fontSize: '0.65rem' }}>Mới nhất</Badge>}
                      </div>
                      <div className="text-muted small mt-1">{new Date(bid.bidTime).toLocaleTimeString('en-US', { hour12: false })}</div>
                    </div>
                    <div className="fw-bold font-monospace fs-5" style={{ color: '#004e64' }}>
                      ${bid.amount.toLocaleString('en-US', { minimumFractionDigits: 2 })}
                    </div>
                  </ListGroup.Item>
                ))
              ) : (
                <ListGroup.Item className="py-5 text-center text-muted fst-italic border-0">
                  Chưa có lượt đặt giá nào. Hãy là người đầu tiên!
                </ListGroup.Item>
              )}
            </ListGroup>
          </div>
        </Col>
      </Row>

      <AutoBidModal
        open={autoBidModalOpen}
        onClose={() => setAutoBidModalOpen(false)}
        sessionId={sessionId}
        currentPrice={sessionDetail.currentPrice}
        minimumIncrement={sessionDetail.minimumIncrement}
        onConfigSaved={handleConfigSaved}
      />
    </Container>
  );
}
