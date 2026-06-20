import { useState, useContext } from 'react';
import { Form, Button, Row, Col, Container, Card } from 'react-bootstrap';
import { Eye, EyeOff } from 'lucide-react';
import { useNavigate, Link } from 'react-router-dom';
import { authApi } from '../../../api/authApi';
import { AuthContext } from '../../../context/AuthContext';
import '../styles/auth.css';

export default function RegisterPage() {
  const navigate = useNavigate();
  const { openAuthModal } = useContext(AuthContext);

  const [registerData, setRegisterData] = useState({ 
    firstName: '', 
    lastName: '', 
    email: '', 
    phoneNumber: '', 
    address: '',
    password: '' 
  });
  const [showRegPass, setShowRegPass] = useState(false);
  const [regError, setRegError] = useState('');
  const [isRegistering, setIsRegistering] = useState(false);

  const handleRegisterSubmit = async (e) => {
    e.preventDefault();
    setRegError('');
    setIsRegistering(true);
    try {
      await authApi.register({
        ...registerData
      });
      navigate('/verify-email', { state: { email: registerData.email, type: 'register' } });
    } catch (err) {
      if (err.message && err.message.includes('User existed')) {
        navigate('/verify-email', { state: { email: registerData.email, type: 'register' } });
      } else {
        setRegError(err.message || 'Registration failed. Please try again.');
      }
    } finally {
      setIsRegistering(false);
    }
  };

  return (
    <div className="register-bg">
      <Container>
        <Row className="justify-content-center">
          <Col md={10} lg={8}>
            <Card className="border-0 glass-panel" style={{ borderRadius: '24px' }}>
              <Row className="g-0">
                {/* Decorative Side */}
                <Col md={4} className="d-none d-md-flex flex-column justify-content-center align-items-center text-white p-4 position-relative" style={{ 
                  background: 'linear-gradient(135deg, #003d5b 0%, #0a9396 100%)',
                  borderTopLeftRadius: '24px',
                  borderBottomLeftRadius: '24px',
                  overflow: 'hidden'
                }}>
                  <div className="position-relative z-1 text-center">
                    <h2 className="fw-bold mb-3" style={{ fontStyle: 'italic', letterSpacing: '2px', fontFamily: 'Impact, sans-serif' }}>
                      ANNEXE
                    </h2>
                    <p className="opacity-90 fs-6">
                      Join our premier auction house and start your collection today.
                    </p>
                  </div>
                  <div className="position-absolute sphere-1" style={{ opacity: 0.5 }}></div>
                  <div className="position-absolute sphere-2" style={{ opacity: 0.5 }}></div>
                </Col>
                
                {/* Form Side */}
                <Col md={8}>
                  <Card.Body className="p-5">
                    <div className="text-center mb-5">
                      <h2 className="fw-bold text-dark mb-2" style={{textShadow: '0 1px 2px rgba(0,0,0,0.1)'}}>Create Account</h2>
                      <p className="text-muted">Fill in your details to get started</p>
                    </div>

                <Form onSubmit={handleRegisterSubmit}>
                  <Row className="mb-3">
                    <Col md={6} className="mb-3 mb-md-0">
                      <Form.Group>
                        <Form.Control
                          type="text"
                          className="premium-input w-100"
                          placeholder="First name"
                          value={registerData.firstName}
                          onChange={(e) => setRegisterData({...registerData, firstName: e.target.value})}
                          required
                          style={{ borderRadius: '8px' }}
                        />
                      </Form.Group>
                    </Col>
                    <Col md={6}>
                      <Form.Group>
                        <Form.Control
                          type="text"
                          className="premium-input w-100"
                          placeholder="Surname"
                          value={registerData.lastName}
                          onChange={(e) => setRegisterData({...registerData, lastName: e.target.value})}
                          required
                          style={{ borderRadius: '8px' }}
                        />
                      </Form.Group>
                    </Col>
                  </Row>

                  <Form.Group className="mb-3">
                    <Form.Control
                      type="email"
                      className="premium-input w-100"
                      placeholder="Mobile number or email address"
                      value={registerData.email}
                      onChange={(e) => setRegisterData({...registerData, email: e.target.value})}
                      required
                      style={{ borderRadius: '8px' }}
                    />
                  </Form.Group>

                  <Form.Group className="mb-3">
                    <Form.Control
                      type="text"
                      className="premium-input w-100"
                      placeholder="Phone Number (Optional)"
                      value={registerData.phoneNumber}
                      onChange={(e) => setRegisterData({...registerData, phoneNumber: e.target.value})}
                      style={{ borderRadius: '8px' }}
                    />
                  </Form.Group>

                  <Form.Group className="mb-3">
                    <Form.Control
                      type="text"
                      className="premium-input w-100"
                      placeholder="Address"
                      value={registerData.address}
                      onChange={(e) => setRegisterData({...registerData, address: e.target.value})}
                      style={{ borderRadius: '8px' }}
                    />
                  </Form.Group>

                  <Form.Group className="mb-4 position-relative">
                    <Form.Control
                      type={showRegPass ? "text" : "password"}
                      className="premium-input w-100"
                      placeholder="New password"
                      value={registerData.password}
                      onChange={(e) => setRegisterData({...registerData, password: e.target.value})}
                      required
                      style={{ borderRadius: '8px' }}
                    />
                    <button 
                      type="button" 
                      className="position-absolute bg-transparent border-0 text-muted"
                      style={{ right: '15px', top: '50%', transform: 'translateY(-50%)' }}
                      onClick={() => setShowRegPass(!showRegPass)}
                    >
                      {showRegPass ? <EyeOff size={20} /> : <Eye size={20} />}
                    </button>
                  </Form.Group>

                  {regError && <div className="text-danger small mb-3 fw-medium px-1">{regError}</div>}

                  <p className="small-text text-muted mb-4" style={{ fontSize: '11px', lineHeight: '1.4' }}>
                    People who use our service may have uploaded your contact information to ANNEXE. <a href="#learnmore">Learn more</a>.
                    <br/><br/>
                    By clicking Register, you agree to our Terms, Privacy Policy and Cookies Policy. You may receive SMS notifications from us and can opt out at any time.
                  </p>

                  <div className="text-center mb-3">
                    <Button type="submit" className="w-100 py-3 fw-bold fs-5 text-white border-0 btn-3d" style={{ backgroundColor: '#003d5b', borderRadius: '8px' }} disabled={isRegistering}>
                    {isRegistering ? 'Processing...' : 'Register'}
                  </Button>
                  </div>

                  <div className="text-center mt-4 border-top pt-4">
                    <span 
                      className="text-primary text-decoration-none fw-medium" 
                      style={{ cursor: 'pointer' }}
                      onClick={() => {
                        navigate('/');
                        openAuthModal();
                      }}
                    >
                      Already have an account? Sign In here
                    </span>
                  </div>
                </Form>
                  </Card.Body>
                </Col>
              </Row>
            </Card>
          </Col>
        </Row>
      </Container>
    </div>
  );
}
