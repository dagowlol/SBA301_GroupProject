import { useState, useContext } from 'react';
import { Modal, Form, Button, Row, Col } from 'react-bootstrap';
import { Eye, EyeOff } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { authApi } from '../../../api/authApi';
import { AuthContext } from '../../../context/AuthContext';
import '../styles/auth.css';

export default function AuthModal({ show, onHide }) {
  const navigate = useNavigate();
  const { loginSuccess } = useContext(AuthContext);

  // Sign In State
  const [loginData, setLoginData] = useState({ email: '', password: '' });
  const [showLoginPass, setShowLoginPass] = useState(false);
  const [loginError, setLoginError] = useState('');
  const [isLoggingIn, setIsLoggingIn] = useState(false);

  const handleLoginSubmit = async (e) => {
    e.preventDefault();
    setLoginError('');
    setIsLoggingIn(true);
    try {
      await authApi.login({
        email: loginData.email,
        password: loginData.password
      });
      // OTP sent, redirect to verify
      onHide();
      navigate('/verify-email', { state: { email: loginData.email, type: 'login' } });
    } catch (err) {
      if (err.message && err.message.includes('not yet activated')) {
        onHide();
        navigate('/verify-email', { state: { email: loginData.email, type: 'login' } });
      } else {
        setLoginError(err.message || 'Login failed. Please try again.');
      }
    } finally {
      setIsLoggingIn(false);
    }
  };



  return (
    <Modal show={show} onHide={onHide} centered size="lg" className="auth-modal" backdropClassName="auth-modal-backdrop">
      <Modal.Body className="p-0 overflow-hidden glass-panel" style={{ borderRadius: '24px' }}>
        <Row className="g-0 h-100">
          {/* Left Branded Side */}
          <Col md={5} className="d-none d-md-flex flex-column justify-content-between text-white p-5 position-relative" style={{ 
            background: 'linear-gradient(135deg, #003d5b 0%, #0a9396 100%)',
          }}>
            <div className="position-relative z-1">
              <h2 className="fw-bold mb-4" style={{ fontStyle: 'italic', letterSpacing: '2px', fontFamily: 'Impact, sans-serif' }}>
                ANNEXE
              </h2>
              <h3 className="fw-bold fs-4 mb-3" style={{textShadow: '0 2px 4px rgba(0,0,0,0.3)'}}>Discover Exclusive Art</h3>
              <p className="opacity-90" style={{ fontSize: '0.95rem', lineHeight: '1.6', textShadow: '0 1px 2px rgba(0,0,0,0.2)' }}>
                Join our premier auction house to bid on rare items, sell your collection, and experience the finest curation of global art.
              </p>
            </div>
            
            {/* Decorative 3D Spheres */}
            <div className="position-absolute sphere-1"></div>
            <div className="position-absolute sphere-2"></div>
          </Col>

          {/* Right Form Side */}
          <Col md={7} className="p-5 position-relative bg-white">
            {/* Close Button */}
            <button 
              onClick={onHide}
              className="position-absolute border-0 bg-transparent text-muted"
              style={{ top: '20px', right: '20px', fontSize: '1.5rem', cursor: 'pointer' }}
            >
              &times;
            </button>

            <div className="d-flex flex-column h-100 justify-content-center">
              <h3 className="auth-form-title mb-4" style={{ color: '#003d5b' }}>Sign In</h3>
              
              <Form onSubmit={handleLoginSubmit}>
                  <Form.Group className="mb-4 position-relative">
                    <Form.Label className="auth-label text-muted small fw-bold text-uppercase">Email address</Form.Label>
                    <Form.Control
                      type="email"
                      className="premium-input w-100"
                      value={loginData.email}
                      onChange={(e) => setLoginData({...loginData, email: e.target.value})}
                      required
                    />
                  </Form.Group>

                  <Form.Group className="mb-4 position-relative">
                    <Form.Label className="auth-label text-muted small fw-bold text-uppercase">Password</Form.Label>
                    <Form.Control
                      type={showLoginPass ? "text" : "password"}
                      className="premium-input w-100"
                      value={loginData.password}
                      onChange={(e) => setLoginData({...loginData, password: e.target.value})}
                      required
                    />
                    <button 
                      type="button" 
                      className="password-toggle"
                      onClick={() => setShowLoginPass(!showLoginPass)}
                    >
                      {showLoginPass ? <EyeOff size={18} /> : <Eye size={18} />}
                    </button>
                  </Form.Group>

                  {loginError && <div className="text-danger small mb-3 fw-medium">{loginError}</div>}

                  <div className="d-flex justify-content-between align-items-center mb-4">
                    <Form.Check 
                      type="checkbox"
                      id="remember-me"
                      label="Remember me"
                      className="small fw-semibold text-muted"
                    />
                  </div>

                  <div className="d-flex justify-content-between align-items-center mb-4">
                    <Button type="submit" className="px-5 py-2 fw-bold border-0 text-white btn-3d" style={{ backgroundColor: '#003d5b', borderRadius: '8px' }} disabled={isLoggingIn}>
                      {isLoggingIn ? 'Wait...' : 'Sign in'}
                    </Button>
                    <a href="#forgot" className="small-link text-muted fw-medium link-hover">Lost your password?</a>
                  </div>
                </Form>

                <div className="mt-2 mb-4">
                  <p className="small-text fw-medium text-muted mb-3">Or sign in with</p>
                  <div className="d-flex gap-3">
                    <button type="button" className="social-btn facebook flex-grow-1 justify-content-center m-0 btn-3d">
                      <span className="fw-bold fs-5 px-1">f</span> Facebook
                    </button>
                    <button type="button" className="social-btn google flex-grow-1 justify-content-center m-0 btn-3d">
                      <span className="fw-bold fs-5 px-1">G</span> Google
                    </button>
                  </div>
                </div>

              <div className="pt-3 border-top mt-auto">
                <p className="small-text fw-medium mb-0">
                  Don't have an account? <span style={{ color: '#003d5b', cursor: 'pointer', fontWeight: 'bold' }} onClick={() => { onHide(); navigate('/register'); }}>Register here</span>
                </p>
              </div>
            </div>
          </Col>
        </Row>
      </Modal.Body>
    </Modal>
  );
}
