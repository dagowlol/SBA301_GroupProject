import { useEffect, useState, useContext } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useQueryClient } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import { Container, Row, Col, Spinner, Alert, Card, Button, Badge, ListGroup, Form, InputGroup } from 'react-bootstrap';
import { ArrowLeft, Crown, Clock, HelpCircle, AlertCircle } from 'lucide-react';

import { AuthContext } from '../../../context/AuthContext';
import { useAuctionSessionDetail } from '../hooks/useAuctionSessionDetail';
import { useBidWebSocket } from '../hooks/useBidWebSocket';
import CountdownTimer from '../components/CountdownTimer';

export default function AuctionRoom() {
  const { sessionId } = useParams();
  const navigate = useNavigate();
  const { user } = useContext(AuthContext);
  const queryClient = useQueryClient();

  // Hooks
  const { data: sessionDetail, isLoading, error } = useAuctionSessionDetail(sessionId);
  const { latestBid, wsError, placeBid, clearWsError } = useBidWebSocket(sessionId);

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

  // Derived states
  const isEnded = isTimeUp || sessionDetail?.status !== 'ACTIVE';
  
  const minRequiredBid = sessionDetail 
    ? (sessionDetail.currentPrice || sessionDetail.reservePrice) + (sessionDetail.minimumIncrement || 0)
    : 0;

  // React Hook Form
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting }
  } = useForm({
    defaultValues: { amount: '' }
  });

  const onSubmit = async (data) => {
    if (!user || !user.id) {
      alert('Vui lòng đăng nhập để đặt giá.');
      return;
    }
    const success = placeBid(user.id, data.amount);
    if (!success) {
      alert('Mất kết nối WebSocket. Vui lòng tải lại trang.');
    } else {
      reset({ amount: '' });
    }
  };

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

          {/* Bid Form */}
          <Card className="border shadow-sm rounded-4 mb-4 border-light">
            <Card.Body className="p-4 p-xl-5">
              <h5 className="fw-bold mb-4 d-flex align-items-center gap-2" style={{ color: '#004e64' }}>
                Tham gia đấu giá
              </h5>
              <Form onSubmit={handleSubmit(onSubmit)}>
                <Form.Group className="mb-4">
                  <InputGroup size="lg" className="shadow-sm">
                    <InputGroup.Text className="bg-white text-muted fw-bold border-end-0 ps-4">$</InputGroup.Text>
                    <Form.Control
                      type="number"
                      step="0.01"
                      className="border-start-0 font-monospace fw-semibold"
                      style={{ boxShadow: 'none' }}
                      placeholder={`Min: ${minRequiredBid.toFixed(2)}`}
                      isInvalid={!!errors.amount}
                      disabled={isEnded}
                      {...register('amount', {
                        required: "Vui lòng nhập số tiền",
                        min: {
                          value: minRequiredBid,
                          message: `Giá đặt phải từ $${minRequiredBid.toFixed(2)} trở lên`
                        }
                      })}
                    />
                    <Form.Control.Feedback type="invalid" className="ps-2">
                      {errors.amount?.message}
                    </Form.Control.Feedback>
                  </InputGroup>
                  <Form.Text className="text-muted small mt-2 ms-2 d-flex gap-2">
                    <span>Bước giá tối thiểu: <strong>${sessionDetail.minimumIncrement?.toFixed(2) || '0.00'}</strong></span>
                  </Form.Text>
                </Form.Group>
                
                <Button 
                  type="submit" 
                  size="lg"
                  className="w-100 fw-bold py-3 rounded-3 shadow-sm text-uppercase tracking-wider transition-all" 
                  style={{ backgroundColor: '#004e64', borderColor: '#004e64', fontSize: '0.95rem' }}
                  disabled={isEnded || isSubmitting}
                >
                  {isSubmitting ? (
                    <Spinner size="sm" animation="border" role="status" />
                  ) : (
                    isEnded ? 'Phiên Đã Đóng' : 'Place Bid'
                  )}
                </Button>
              </Form>
            </Card.Body>
          </Card>

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
    </Container>
  );
}
